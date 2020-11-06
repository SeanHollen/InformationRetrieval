package HW2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public class Tokenizer {

  // used
  private HashMap<String, String> wordSubstitutions;
  private HashSet<String> stopwords;
  private HashMap<String, String> documents;
  // mappings
  private HashMap<Integer, String> docHashes;
  private HashMap<Integer, String> tokensHash;
  // derived
  private int numTokens;
  private double avgDocLength;
  private HashMap<Integer, Integer> docLengthsMap;

  public Tokenizer() {
    // Token Hash -> Token
    tokensHash = new HashMap<Integer, String>();
    // Document Hash -> Document
    docHashes = new HashMap<Integer, String>();
    // Document ID -> Document text
    documents = new HashMap<String, String>();
    // Document Hash -> Document Length
    docLengthsMap = new HashMap<Integer, Integer>();
  }

  public Tokenizer(HashSet<String> stopwords, HashMap<String, String> wordSubstitutions) {
    this();
    this.stopwords = stopwords;
    this.wordSubstitutions = wordSubstitutions;
  }

  public void putDocuments(HashMap<String, String> documents) {
    for (String docId : documents.keySet()) {
      docHashes.put(docId.hashCode(), docId);
    }
    this.documents.putAll(documents);
  }

  public void putDocument(String docId, String text) {
    docHashes.put(docId.hashCode(), docId);
    this.documents.put(docId, text);
  }

  public void index(String outDir) {
    Object[] docKeys = documents.keySet().toArray();
    ArrayList<TermPosition> newTokens = new ArrayList<TermPosition>();
    HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> newSortedTokens;
    numTokens = 0;
    System.out.println("indexing " + docKeys.length + " documents");
    for (int i = 0; i < docKeys.length; i++) {
      String key = (String) docKeys[i];
      int docHash = key.hashCode();
      newTokens.addAll(tokenize(documents.get(key), docHash, true));
      numTokens += newTokens.size();
      docLengthsMap.put(docHash, newTokens.size());
      // stop wall!! Only pass if i is a nonzero divisor of 1000, or the last element
      if (i != docKeys.length - 1 && !(i % 1000 == 0 && i != 0)) {
        continue;
      }
      System.out.println("Creating new catalog and inverted index file, " + i + " files indexed");
      System.out.println("File name: " + (int) Math.ceil((double) i / 1000) + ".txt");
      newSortedTokens = toSortedForm(newTokens);
      newTokens = new ArrayList<TermPosition>();
      try {
        // file setup {
        String catPath = outDir + "/catalogs/" + (int) Math.ceil((double) i / 1000) + ".txt";
        String invPath = outDir + "/invList/" + (int) Math.ceil((double) i / 1000) + ".txt";
        File catalog = new File(catPath);
        File invIndex = new File(invPath);
        boolean created = catalog.createNewFile() && invIndex.createNewFile();
        FileWriter catWriter = new FileWriter(catPath);
        FileWriter indexWriter = new FileWriter(invPath);
        // }
        int lineNum = 0;
        for (int tokenHash : newSortedTokens.keySet()) {
          lineNum++;
          HashMap<Integer, ArrayList<Integer>> tokens = newSortedTokens.get(tokenHash);
          StringBuilder tokensBuilder = new StringBuilder();
          for (int doc : tokens.keySet()) {
            ArrayList<Integer> tokenDocs = tokens.get(doc);
            StringBuilder positionsBuilder = new StringBuilder();
            for (int pos : tokenDocs) {
              positionsBuilder.append(pos);
              positionsBuilder.append(",");
            }
            tokensBuilder.append(doc + "|" + tokenDocs.size() + "|" + positionsBuilder.toString() + ";");
          }
          String termString = tokenHash + "=" + tokensBuilder.toString();
          indexWriter.write(termString + "\n");
          catWriter.write(tokenHash + " " + tokensHash.get(tokenHash) + " " + lineNum + "\n");
        }
        catWriter.close();
        indexWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    avgDocLength = (double) numTokens / (double) docHashes.size();
    System.out.println("Vocabulary size: " + tokensHash.size());
    System.out.println("Number of docs: " + documents.size());
    System.out.println("Aggregate number of Tokens: " + numTokens);
    System.out.println("Average Number of Tokens: " + avgDocLength);
    putToFile(outDir + "/docIds.txt", docHashes);
    putToFile(outDir + "/tokenIds.txt", tokensHash);
    putToFile(outDir + "/docLengths.txt", docLengthsMap);
    putToFile(outDir + "/aggInfo.txt", new Integer[]
            {tokensHash.size(), documents.size(), numTokens, (int) avgDocLength});
  }

  private void putToFile(String location, Object toPut) {
    try {
      File file = new File(location);
      if (file.createNewFile()) {
        System.out.println("created a file: " + location);
      } else {
        System.out.println("failed to create a file, likely already exists, and will write over");
      }
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
      oos.writeObject(toPut);
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void merge(String dir) {
    File[] catalogsArr = new File(dir + "/catalogs").listFiles();
    File[] invListsArr = new File(dir + "/invList").listFiles();
    if (catalogsArr == null || invListsArr == null) {
      throw new IllegalArgumentException("failure to find files in dir");
    }
    ArrayList<File> catalogs = new ArrayList<File>(Arrays.asList(catalogsArr));
    ArrayList<File> invLists = new ArrayList<File>(Arrays.asList(invListsArr));
    int length = invLists.size();
    while (catalogs.size() > 1 && invLists.size() > 1) {
      int l = catalogs.size();
      for (int i = 0; i < l; i += 2) {
        try {
          // Readers
          File cat1 = catalogs.get(i);
          BufferedReader cat1Reader = new BufferedReader(new FileReader(cat1));
          File cat2 = catalogs.get(i + 1);
          BufferedReader cat2Reader = new BufferedReader(new FileReader(cat2));
          File invList1 = invLists.get(i);
          BufferedReader invList1Reader = new BufferedReader(new FileReader(invList1));
          File invList2 = invLists.get(i + 1);
          BufferedReader invList2Reader = new BufferedReader(new FileReader(invList2));
          // New files
          length++;
          String catPath = dir + "/catalogs/" + length + ".txt";
          String invPath = dir + "/invList/" + length + ".txt";
          File newCatalog = new File(invPath);
          File newInvIndex = new File(invPath);
          if (newCatalog.createNewFile() && newInvIndex.createNewFile()) {
            System.out.println("2 files created");
          } else {
            System.out.println("At least one file not created (perhaps already exists)");
          }
          // Writers
          FileWriter catWriter = new FileWriter(catPath);
          FileWriter indexWriter = new FileWriter(invPath);
          // Cat ArrayLists
          String line;
          ArrayList<String[]> cat1Arr = new ArrayList<String[]>();
          ArrayList<String[]> cat2Arr = new ArrayList<String[]>();
          while ((line = cat1Reader.readLine()) != null) {
            cat1Arr.add(line.split(" ", 2));
          }
          while ((line = cat2Reader.readLine()) != null) {
            cat2Arr.add(line.split(" ", 2));
          }

          // TACTIC 1: Merge 2 sorted lists
          int catPlace = 0;
          int place1 = 0;
          int place2 = 0;
          String newTerm;
          String oldTerm = "";
          String newIndexLine;
          double evaluation;
          while (place1 < cat1Arr.size() && place2 < cat2Arr.size()) {
            if (place1 == cat1Arr.size()) {
              evaluation = 1;
            } else if (place2 == cat2Arr.size()) {
              evaluation = -1;
            } else {
              evaluation = cat1Arr.get(place1)[1].compareTo(cat2Arr.get(place2)[1]);
            }
            if (evaluation <= 0) {
              newTerm = cat1Arr.get(place1)[0];
              newIndexLine = invList1Reader.readLine();
              place1++;
            } else {
              newTerm = cat2Arr.get(place2)[0];
              newIndexLine = invList2Reader.readLine();
              place2++;
            }
            if (newTerm.equals(oldTerm)) {
              indexWriter.write(newIndexLine.split("=", 2)[1]);
            } else {
              if (catPlace != 0) indexWriter.write("\n");
              catPlace++;
              indexWriter.write(newIndexLine);
              catWriter.write(newTerm + " " + tokensHash.get(Integer.parseInt(newTerm))
                      + " " + catPlace + "\n");
            }
            oldTerm = newTerm;
          }
          // TACTIC 2: Merge as instructed
          // todo: implement

          // Update lists of Files
          catalogs.add(newCatalog);
          invLists.add(newInvIndex);
          catalogs.remove(cat1);
          catalogs.remove(cat2);
          invLists.remove(invList1);
          invLists.remove(invList2);
          catWriter.close();
          indexWriter.close();
          if (!(cat1.delete() && cat2.delete() && invList1.delete() && invList2.delete())) {
            System.out.println("failed to wipe one of the old files");
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public ArrayList<TermPosition> tokenize(String document, int from, boolean doStemming) {
    String[] terms = document.toLowerCase()
            .replaceAll("[^\\w\\s]", "").split("\\s");
    ArrayList<TermPosition> tokensList = new ArrayList<TermPosition>();
    int place = 0;
    for (String token : terms) {
      if (token.equals("")) continue;
      if (stopwords.contains(token)) continue;
      place++;
      if (doStemming && wordSubstitutions.containsKey(token)) {
        token = wordSubstitutions.get(token);
      }
      Integer tokenHash = token.hashCode();
      if (!tokensHash.containsKey(tokenHash)) {
        tokensHash.put(tokenHash, token);
      }
      tokensList.add(new TermPosition(tokenHash, tokensHash.get(tokenHash), from, place));
    }
    // System.out.println("num tokens: " + tokensList.size());
    return tokensList;
  }

  private LinkedHashMap<Integer, HashMap<Integer, ArrayList<Integer>>> toSortedForm(
          ArrayList<TermPosition> termPositions) {
    Collections.sort(termPositions);
    LinkedHashMap<Integer, HashMap<Integer, ArrayList<Integer>>> toReturn
            = new LinkedHashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();
    for (TermPosition tp : termPositions) {
      if (!toReturn.containsKey(tp.getTermHash())) {
        toReturn.put(tp.getTermHash(), new HashMap<Integer, ArrayList<Integer>>());
      }
      if (!toReturn.get(tp.getTermHash()).containsKey(tp.getDocHash())) {
        toReturn.get(tp.getTermHash()).put(tp.getDocHash(), new ArrayList<Integer>());
      }
      toReturn.get(tp.getTermHash()).get(tp.getDocHash()).add(tp.getPosition());
    }
    // System.out.println("sorted form: " + toReturn);
    return toReturn;
  }

  public void clear(String dir) {
    File[] catalogsArr = new File(dir + "/catalogs").listFiles();
    File[] invListsArr = new File(dir + "/invList").listFiles();
    if (catalogsArr == null || invListsArr == null) {
      throw new IllegalArgumentException("nulls");
    }
    for (File file : catalogsArr) {
      file.delete();
    }
    for (File file : invListsArr) {
      file.delete();
    }
  }

}
