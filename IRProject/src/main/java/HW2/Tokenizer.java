package HW2;

import org.javatuples.Triplet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Tokenizer {

  private HashMap<String, String> wordSubstitutions;
  private HashSet<String> stopwords;
  private HashMap<Integer, String> docHashes;
  private HashMap<Integer, String> tokens;
  private HashMap<Integer, Integer> vocabSize;
  private int numTokens;
  private double avgNumTokens;
  private HashMap<String, String> documents;

  public Tokenizer() {
    // Token Hash -> Token
    tokens = new HashMap<Integer, String>();
    // Token Hash -> count
    vocabSize = new HashMap<Integer, Integer>();
    // Document Hash -> Document
    docHashes = new HashMap<Integer, String>();
    // Document ID -> Document text
    documents = new HashMap<String, String>();
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
    int count = 0;
    for (String docId : documents.keySet()) {
      count++;
      int docHash = docId.hashCode();
      ArrayList<Triplet> tokens = tokenize(documents.get(docId), docHash, true);
      numTokens += tokens.size();
      vocabSize.put(docHash, tokens.size());
      try {
        String catPath = outDir + "/catalogs/" + count + ".txt";
        File catalog = new File(catPath);
        if (catalog.createNewFile()) {
          System.out.println("File created: " + catalog.getName());
        } else {
          System.out.println("Not created");
        }
        FileWriter catWriter = new FileWriter(catPath);
        String invPath = outDir + "/invList/" + count + ".txt";
        File invIndex = new File(invPath);
        if (invIndex.createNewFile()) {
          System.out.println("File created: " + invIndex.getName());
        } else {
          System.out.println("Not created");
        }
        FileWriter indexWriter = new FileWriter(invPath);
        catWriter.write("");
        indexWriter.write("");
        catWriter.close();
        indexWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    avgNumTokens = (double) numTokens / (double) docHashes.size();
  }

  public void merge() {

  }

  public ArrayList<Triplet> tokenize(String document, int from, boolean doStemming) {
    String[] terms = document.toLowerCase()
            .replaceAll("[^\\w\\s]", "").split("\\s");
    ArrayList<Triplet> tokensList = new ArrayList<Triplet>();
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
      tokensList.add(Triplet.with(tokenHash, from, place));
    }
    return tokensList;
  }

  public String getDocName(int hash) {
    return docHashes.get(hash);
  }

  public String getToken(int hash) {
    return tokens.get(hash);
  }

  public int vocabSizeOf(int docHash) {
    return vocabSize.get(docHash);
  }

  public double getAvgNumTokens() {
    return avgNumTokens;
  }

}
