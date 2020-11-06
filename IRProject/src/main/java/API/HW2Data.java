package API;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import HW2.TermPosition;
import HW2.Tokenizer;

public class HW2Data implements Data {

  private HashMap<Integer, String> docHashes;
  private HashMap<Integer, String> tokens;

  // HashMap<term, dfScore> How many documents a term appears in
  private HashMap<String, Double> dfScores;
  // HashMap<docId, HashMap<term, tfScore>> How frequently a term appears in a document
  private HashMap<String, HashMap<String, Double>> tfScores;
  // HashMap<term, aggTfScore> For each given term, its total frequency across all documents
  private HashMap<String, Double> tfScores_agg;
  // HashMap<docId, docLength> The length of each document
  private HashMap<Integer, Integer> docLengths;
  // Average length of docs
  private double avgDocLengths;
  // Total number of unique terms in the collection
  private double vocabSize;
  // Total lengths of docs
  private double totalDocLengths;
  // HashMap<queryId, ArrayList<term>> The list of stemmed terms in each query
  private HashMap<Integer, ArrayList<String>> stemmed;

  private Tokenizer tokenized;
  private String path = "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/mergeDataPath/";

  public HW2Data(Tokenizer tokenized) {
    this.tokenized = tokenized;
    dfScores = new HashMap<String, Double>();
    tfScores = new HashMap<String, HashMap<String, Double>>();
    tfScores_agg = new HashMap<String, Double>();
    docLengths = new HashMap<Integer, Integer>();
  }

  public void fetch(HashMap<Integer, String> queries, ArrayList<String> docIds) {

    try {
      ObjectInputStream ois1 = new ObjectInputStream(new FileInputStream(path
              + "aggData.txt"));
      Integer[] agg = (Integer[]) ois1.readObject();
      vocabSize = (double) agg[0];
      System.out.println("vocab size: " + vocabSize);
      totalDocLengths = (double) agg[2];
      System.out.println("total doc lengths:  " + totalDocLengths);
      avgDocLengths = (double) agg[3];
      System.out.println("average doc lengths: " + avgDocLengths);
      ois1.close();

      ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(path + "docIds.txt"));
      docHashes = (HashMap<Integer, String>) ois2.readObject();
      System.out.println(docHashes);
      ois2.close();

      ObjectInputStream ois3 = new ObjectInputStream(new FileInputStream(path + "docIds.txt"));
      tokens = (HashMap<Integer, String>) ois3.readObject();
      System.out.println("tokens size: " + tokens.size());
      ois3.close();

      ObjectInputStream ois4 = new ObjectInputStream(new FileInputStream(path + "docIds.txt"));
      docLengths = (HashMap<Integer, Integer>) ois4.readObject();
      System.out.println("doc lengths " + docLengths.size());
      ois4.close();

      File invListFile = new File(path + "invList");
      BufferedReader reader = new BufferedReader(new FileReader(invListFile));
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.equals("") || line.equals("\n")) continue;
        String[] parse1 = line.split("=");
        String tokenHash = parse1[0];
        String[] parse2 = parse1[1].split(";");
        dfScores.put(tokenHash, (double) parse2.length);
        tfScores.put(tokenHash, new HashMap<String, Double>());
        for (String s : parse2) {
          String[] parse3 = s.split("\\|");
          double tfScore = (double) Integer.parseInt(parse3[1]);
          tfScores.get(tokenHash).put(parse3[0], tfScore);
          tfScores_agg.put(tokenHash, tfScores_agg.get(tokenHash) + tfScore);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    stemmed = new HashMap<Integer, ArrayList<String>>();
    for (int i : queries.keySet()) {
      ArrayList<TermPosition> tps = tokenized.tokenize(queries.get(i), i,true);
      ArrayList<String> terms = new ArrayList<String>();
      for (TermPosition tp : tps) {
        terms.add("" + tp.getTermHash());
      }
      stemmed.put(i, terms);
    }
  }

  public double tf(String docId, String term) {
    if (!tfScores.get(docId).containsKey(term)) {
      return 0;
    }
    return tfScores.get(docId).get(term);
  }

  public double tf_agg(String term) {
    if (!tfScores_agg.containsKey(term)) {
      tfScores_agg.put(term, (double) 0);
      return 0;
    }
    return tfScores_agg.get(term);
  }

  public double df(String term) {
    if (!dfScores.containsKey(term)) {
      System.out.println("Anomaly: none of the documents contain the term " + term);
      dfScores.put(term, (double) 0);
    }
    return dfScores.get(term);
  }

  public double docLen(String document) {
    if (!docLengths.containsKey(Integer.parseInt(document))) {
      docLengths.put(Integer.parseInt(document), 0);
    }
    return (double) docLengths.get(Integer.parseInt(document));
  }

  public double vocabSize() {
    return vocabSize;
  }

  public double avgDocLengths() {
    return avgDocLengths;
  }

  public double totalDocLengths() {
    return totalDocLengths;
  }

  public HashMap<Integer, ArrayList<String>> getStemmed() {
    return stemmed;
  }
}
