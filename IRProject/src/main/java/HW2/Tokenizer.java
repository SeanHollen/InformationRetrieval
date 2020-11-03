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
  private HashMap<Integer, String> tokens;
  // derived
  private int numTokens;
  private double avgDocLength;
  private HashMap<Integer, Integer> docLengthsMap;

  public Tokenizer() {
    // Token Hash -> Token
    tokens = new HashMap<Integer, String>();
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
    int outDirLen = outDir.length();
    Object[] docKeys = documents.keySet().toArray();
    ArrayList<TermPosition> newTokens = new ArrayList<TermPosition>();
    HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> newSortedTokens;
    numTokens = 0;
    for (int i = 0; i < docKeys.length; i++) {
      String key = (String) docKeys[i];
      int docHash = key.hashCode();
      newTokens.addAll(tokenize(documents.get(key), docHash, true));
      numTokens += newTokens.size();
      docLengthsMap.put(docHash, newTokens.size());
      // stop wall!! Only pass if i is a nonzero divisor of 1000, or the last element
      if ((!(i % 1000 == 0 && i != 0)) && i != docKeys.length - 1) {
        continue;
      }
      System.out.println("Creating new catalog and inverted index file");
      newSortedTokens = toSortedForm(newTokens);
      newTokens = new ArrayList<TermPosition>();
      try {
        // file setup {
        String catPath = outDir + "/catalogs/" + (int) Math.ceil((double) (i / 1000) + 1) + ".txt";
        String invPath = outDir + "/invList/" + (int) Math.ceil((double) (i / 1000) + 1) + ".txt";
        File catalog = new File(catPath);
        File invIndex = new File(invPath);
        if (catalog.createNewFile() && invIndex.createNewFile()) {
          System.out.println("2 files created");
        } else {
          System.out.println("At least one 2 file not created (perhaps already exists)");
        }
        FileWriter catWriter = new FileWriter(catPath);
        FileWriter indexWriter = new FileWriter(invPath);
        // }
        int fileSize = 0;
        int newFileSize;
        for (int tokenHash : newSortedTokens.keySet()) {
          HashMap<Integer, ArrayList<Integer>> tokens = newSortedTokens.get(tokenHash);
          StringBuilder tokensBuilder = new StringBuilder();
          for (int doc : tokens.keySet()) {
            ArrayList<Integer> tokenDocs = tokens.get(doc);
            StringBuilder positionsBuilder = new StringBuilder();
            for (int pos : tokenDocs) {
              positionsBuilder.append(pos);
              positionsBuilder.append(",");
            }
            tokensBuilder.append(doc);
            tokensBuilder.append("|");
            tokensBuilder.append(tokenDocs.size());
            tokensBuilder.append("|");
            tokensBuilder.append(positionsBuilder.toString());
            tokensBuilder.append(";");
          }
          String termString = tokenHash + "=" + tokensBuilder.toString();
          indexWriter.write(termString + "\n");
          newFileSize = fileSize + termString.length();
          catWriter.write(tokenHash + " " + fileSize + " " + newFileSize
                  + " " + invPath.substring(outDirLen + 1, invPath.length()) + "\n");
          fileSize = newFileSize;
        }
        catWriter.close();
        indexWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    avgDocLength = (double) numTokens / (double) docHashes.size();
    System.out.println("Vocabulary size: " + tokens.size());
    System.out.println("Number of docs: " + documents.size());
    System.out.println("Aggregate number of Tokens: " + numTokens);
    System.out.println("Average Number of Tokens: " + avgDocLength);
    putToFile(outDir + "/docIds.txt", docHashes);
    putToFile(outDir + "/tokenIds.txt", tokens);
    putToFile(outDir + "/docLengths.txt", docLengthsMap);
    putToFile(outDir + "/aggInfo.txt", new Integer[]
            {tokens.size(), documents.size(), numTokens, (int) avgDocLength});
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
    List<File> catalogs = Arrays.asList(catalogsArr);
    List<File> invLists = Arrays.asList(invListsArr);
    int length = invLists.size();
    while (catalogs.size() > 1 && invLists.size() > 1) {
      int l = catalogs.size();
      for (int i = 0; i < l; i += 2) {
        File cat1 = catalogs.get(i);
        File cat2 = catalogs.get(i+1);
        File inv1List = invLists.get(i);
        File inv2List = invLists.get(i+1);
        HashMap<Integer, String> cat1Hash = new HashMap<Integer, String>();
        HashMap<Integer, String> cat2Hash = new HashMap<Integer, String>();
        try {
          String line;
          String[] parse;
          BufferedReader readerCat1 = new BufferedReader(new FileReader(cat1));
          while ((line = readerCat1.readLine()) != null) {
            parse = line.split(" ", 2);
            cat1Hash.put(Integer.parseInt(parse[0]), parse[2]);
          }
          BufferedReader readerCat2 = new BufferedReader(new FileReader(cat2));
          while ((line = readerCat2.readLine()) != null) {
            parse = line.split(" ", 2);
            cat2Hash.put(Integer.parseInt(parse[0]), parse[2]);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
        length++;
        String catPath = dir + "/catalogs/" + i + ".txt";
        String invPath = dir + "/invList/" + length + ".txt";
        File newCatalog = new File(invPath);
        File newInvIndex = new File(invPath);
        if (newCatalog.createNewFile() && newInvIndex.createNewFile()) {
          System.out.println("2 files created");
        } else {
          System.out.println("At least one file not created (perhaps already exists)");
        }
        FileWriter catWriter = new FileWriter(catPath);
        FileWriter indexWriter = new FileWriter(invPath);
        for (Integer id : cat1Hash.keySet()) {
          if (cat2Hash.containsKey(id)) {
            catWriter.write(id + " " + cat1Hash + cat2Hash); // todo is this correct?
            cat2Hash.remove(id);
          } else {
            catWriter.write(id + " " + cat1Hash.get(id));
          }
        }
        for (Integer id : cat2Hash.keySet()) {
          catWriter.write(id + " " + cat2Hash.get(id));
        }
        catWriter.write("");
        indexWriter.write("");
        catalogs.add(newCatalog);
        invLists.add(newInvIndex);
        catalogs.remove(cat1);
        catalogs.remove(cat2);
        invLists.remove(inv1List);
        invLists.remove(inv2List);
        catWriter.close();
        indexWriter.close();
        if (!(cat1.delete() && cat2.delete() && inv1List.delete() && inv2List.delete())) {
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
      if (stopwords.contains(token)) continue;
      place++;
      if (doStemming && wordSubstitutions.containsKey(token)) {
        token = wordSubstitutions.get(token);
      }
      Integer tokenHash = token.hashCode();
      if (!tokens.containsKey(tokenHash)) {
        tokens.put(tokenHash, token);
      }
      tokensList.add(new TermPosition(tokenHash, from, place));
    }
    // System.out.println("num tokens: " + tokensList.size());
    return tokensList;
  }

  private LinkedHashMap<Integer, HashMap<Integer, ArrayList<Integer>>> toSortedForm(
          ArrayList<TermPosition> termPositions) {
    // Collections.sort(termPositions); // todo i have no damn idea why this sort doesn't work
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

}
