package HW2;

import org.javatuples.Triplet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Tokenizer {

  private HashMap<String, String> wordSubstitutions;
  private HashSet<String> stopwords;
  private HashMap<Integer, String> docHashes;
  // todo does tokens need to be written to memory?
  private HashMap<Integer, String> tokens;
  private HashMap<Integer, Integer> vocabSize;
  private int numTokens;
  private double avgNumTokens;
  private HashMap<String, String> documents;

  public Tokenizer() {
    // ID -> token mapping
    tokens = new HashMap<Integer, String>();
  }

  public Tokenizer(HashSet<String> stopwords, HashMap<String, String> wordSubstitutions) {
    this();
    this.stopwords = stopwords;
    this.wordSubstitutions = wordSubstitutions;
  }

  public void putDocuments(HashMap<String, String> documents) {
    // ID -> document mapping
    docHashes = new HashMap<Integer, String>();
    for (String docId : documents.keySet()) {
      docHashes.put(docId.hashCode(), docId);
    }
    this.documents = documents;
  }

  // todo: this
  public void index(String outDir) {
    vocabSize = new HashMap<Integer, Integer>();
    for (String docId : documents.keySet()) {
      int docHash = docId.hashCode();
      ArrayList<Triplet> tokens = tokenize(documents.get(docId), docHash, true);
      numTokens += tokens.size();
      vocabSize.put(docHash, tokens.size());
      }
    avgNumTokens = (double) numTokens / (double) docHashes.size();
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
