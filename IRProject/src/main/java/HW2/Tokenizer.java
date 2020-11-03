package HW2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

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
    this.documents.put(docId, text);
  }

  public void index(String outDir) {
    String[] docKeys = (String[]) documents.keySet().toArray();
    for (int i = 1; 1000 < documents.size() / i; i++) {
      ArrayList<TermPosition> newTokens = new ArrayList<TermPosition>();
      for (String docKey : docKeys) {
        int docHash = docKey.hashCode();
        newTokens.addAll(tokenize(documents.get(docKey), docHash, true));
        numTokens += newTokens.size();
        docLengthsMap.put(docHash, newTokens.size());
      }
      try {
        // boilerplate
        String catPath = outDir + "catalogs/" + i + ".txt";
        String invPath = outDir + "invList/" + i + ".txt";
        File catalog = new File(catPath);
        File invIndex = new File(invPath);
        if (catalog.createNewFile() && invIndex.createNewFile()) {
          System.out.println("2 files created");
        } else {
          System.out.println("At least one file not created (perhaps already exists)");
        }
        FileWriter catWriter = new FileWriter(catPath);
        FileWriter indexWriter = new FileWriter(invPath);

        int fileSize = 0;
        int newFileSize;
        Collections.sort(newTokens);
        ArrayList<TermPosition> matchingTokens = new ArrayList<TermPosition>();
        int match = -1;
        for (TermPosition triplet : newTokens) {
          if (matchingTokens.size() == 0 || triplet.getTermHash() == match) {
            matchingTokens.add(triplet);
          } else {
            StringBuilder b = new StringBuilder(triplet.getTermHash() + "=");
//            for (int x = 0; i < matchingTokens.; x++) {
//              b.append(x);
//              b.append("|");
//              b.append(matchingTokens.size());
//              b.append("|");
//              for (TermPosition tp : matchingTokens) {
//                b.append(tp.getPosition());
//                b.append(",");
//              }
//              b.append(";");
//            }
            indexWriter.write(b.toString() + "\n");
            newFileSize = fileSize + b.toString().length();
            catWriter.write(triplet.getTermHash() + " " + fileSize + " " + newFileSize
                    + " " + invPath + "\n");
            fileSize = newFileSize;
            matchingTokens = new ArrayList<TermPosition>();
            matchingTokens.add(triplet);
            match = -1;
          }
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
    putToFile(outDir + "docIds.txt", docHashes);
    putToFile(outDir + "tokenIds.txt", tokens);
    putToFile(outDir + "docLengths.txt", docLengthsMap);
    putToFile(outDir + "aggInfo.txt", new Integer[]
            {tokens.size(), documents.size(), numTokens, (int) avgDocLength});
  }

  private void putToFile(String location, Object toPut) {
    try {
      File file = new File(location);
      if (file.createNewFile()) {
        System.out.println("created a file");
      } else {
        System.out.println("failed to create a file");
      }
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
      oos.writeObject(toPut);
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void merge(String dir) {
    File[] catalogs = new File(dir + "/catalogs").listFiles();
    File[] invLists = new File(dir + "/invList").listFiles();
    if (catalogs == null || invLists == null) {
      throw new IllegalArgumentException("failure to find files in dir");
    }
    while (catalogs.length > 1 && invLists.length > 1) {
      for (int i = 0; i < catalogs.length; i += 2) {
        String cat1Name = catalogs[i].getName();
        String cat2Name = catalogs[i+1].getName();
        String inv1Lists = invLists[i].getName();
        String inv2Lists = invLists[i+1].getName();
        //todo
      }
    }
    catalogs = new File(dir + "/catalogs").listFiles();
    invLists = new File(dir + "/invList").listFiles();
    if (catalogs == null || invLists == null) {
      throw new IllegalArgumentException("failure to find files in dir");
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
    return tokensList;
  }

  public String getDocName(int hash) {
    return docHashes.get(hash);
  }

  public String getToken(int hash) {
    return tokens.get(hash);
  }

  public double getAvgNumTokens() {
    return avgDocLength;
  }

  public int docLength(int docHash) {
    return docLengthsMap.get(docHash);
  }

}
