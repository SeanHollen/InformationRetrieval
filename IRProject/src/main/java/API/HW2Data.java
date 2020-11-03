package API;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import HW2.TermPosition;
import HW2.Tokenizer;

public class HW2Data implements Data {

  // HashMap<term, dfScore> How many documents a term appears in
  private HashMap<String, Double> dfScores;
  // HashMap<docId, HashMap<term, tfScore>> How frequently a term appears in a document
  private HashMap<String, HashMap<String, Double>> tfScores;
  // HashMap<term, aggTfScore> For each given term, its total frequency across all documents
  private HashMap<String, Double> tfScores_agg;
  // HashMap<docId, docLength> The length of each document
  private HashMap<String, Double> docLengths;
  // Average length of docs
  private double avgDocLengths;
  // Total number of unique terms in the collection
  private double vocabSize = 0;
  // Total lengths of docs
  private double totalDocLengths;
  // HashMap<queryId, ArrayList<term>> The list of stemmed terms in each query
  private HashMap<Integer, ArrayList<String>> stemmed;

  private Tokenizer tokenized;
  private String invListPath = "";
  private String aggDataPath = "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/mergeDataPath/aggData.txt";

  public HW2Data(Tokenizer tokenized) {
    this.tokenized = tokenized;
    dfScores = new HashMap<String, Double>();
    tfScores = new HashMap<String, HashMap<String, Double>>();
    tfScores_agg = new HashMap<String, Double>();
    docLengths = new HashMap<String, Double>();
  }

  public void fetch(HashMap<Integer, String> queries, ArrayList<String> docIds) {

    // todo: fetch data {
    HashMap<Integer, String> docHashes = new HashMap<Integer, String>();
    HashMap<Integer, String> tokens = new HashMap<Integer, String>();
    HashMap<Integer, Integer> docLengthsMap = new HashMap<Integer, Integer>();
    // }

    try {
      File invListFile = new File(invListPath);
      BufferedReader reader = new BufferedReader(new FileReader(invListFile));
      String line;
      while ((line = reader.readLine()) != null) {
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
      // todo I'm not sure that it will actually get read in that form
      File aggDataFile = new File(aggDataPath);
      BufferedReader aggDataReader = new BufferedReader(new FileReader(aggDataFile));
      String aggDataText = aggDataReader.readLine();
      String[] aggData = aggDataText.split(" ");
      // was generated with: tokens.size(), documents.size(), numTokens, (int) avgDocLength
      vocabSize = Integer.parseInt(aggData[0]);
      totalDocLengths = Integer.parseInt(aggData[2]);
      avgDocLengths = Integer.parseInt(aggData[3]);
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
    if (!docLengths.containsKey(document)) {
      docLengths.put(document, (double) 0);
    }
    return docLengths.get(document);
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
