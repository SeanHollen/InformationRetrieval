package Ranking;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import Indexing.*;
import java.io.RandomAccessFile;

public class DataPrivate implements Data {

  // in case
  private HashMap<String, Integer> docHashes;
  private HashMap<Integer, String> tokens;
  private final String path = "IndexData";

  // HashMap<docId, docLength> The length of each document
  private HashMap<Integer, Integer> docLengthsMap;
  // Average length of docs
  private double avgDocLengths;
  // Total number of unique terms in the collection
  private double vocabSize;
  // Total lengths of docs
  private double totalDocLengths;

  private HashMap<String, HashMap<String, Double>> tfScores;
  private HashMap<String, Double> dfScores;
  private HashMap<String, Double> tfScores_agg;

  // function objects, etc.
  private PrivateIndexing tokenized;
  private RandomAccessFile invListReader;
  private HashMap<String, CatalogEntry> catalog;

  public DataPrivate(PrivateIndexing tokenized) {
    this.tokenized = tokenized;
    docLengthsMap = new HashMap<>();
  }

  public void loadToMemory(ArrayList<String> docIds) {
    readSimpleFiles();
    // docIds argument not used
    File[] directory1 = new File(path + "/invList").listFiles();
    if (directory1 == null || directory1.length == 0) {
      throw new IllegalArgumentException("No inv list file found");
    }
    File invListFile = directory1[directory1.length - 1];

    File[] directory2 = new File(path + "/catalogs").listFiles();
    if (directory2 == null || directory2.length == 0) {
      throw new IllegalArgumentException("No inv list file found");
    }

    File catFile = directory2[directory2.length - 1];
    BufferedReader catReader = null;
    try {
      invListReader = new RandomAccessFile(invListFile, "r");
      catReader = new BufferedReader(new FileReader(catFile));
    } catch (Exception e) {
      System.out.println("problem in connecting to index data");
      e.printStackTrace();
    }

    catalog = new HashMap<>();
    String nextCatLine;
    try {
      while ((nextCatLine = catReader.readLine()) != null) {
        String[] split = nextCatLine.split("\\s");
        catalog.put(String.valueOf(split[0]), new CatalogEntry(Integer.parseInt(split[0]), split[1],
                Integer.parseInt(split[2]), Integer.parseInt(split[3])));
      }
    } catch (IOException e) {
      System.out.println("problem building catalog hashmap");
      e.printStackTrace();
    }
  }

  private void readSimpleFiles() {
    try {
      // vocabSize, totalDocLengths, avgDocLengths
      ObjectInputStream ois1 = new ObjectInputStream(new FileInputStream(path
              + "/aggInfo.txt"));
      Integer[] agg = (Integer[]) ois1.readObject();
      vocabSize = (double) agg[0];
      System.out.println("vocab size: " + vocabSize);
      totalDocLengths = (double) agg[2];
      System.out.println("total doc lengths:  " + totalDocLengths);
      avgDocLengths = (double) agg[3];
      System.out.println("average doc lengths: " + avgDocLengths);
      ois1.close();
      // docHashes
      ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(
              path + "/docIds.txt"));
      docHashes = (HashMap<String, Integer>) ois2.readObject();
      ois2.close();
      // tokens
      ObjectInputStream ois3 = new ObjectInputStream(new FileInputStream(
              path + "/docIds.txt"));
      tokens = (HashMap<Integer, String>) ois3.readObject();
      ois3.close();
      // docLengths
      ObjectInputStream ois4 = new ObjectInputStream(new FileInputStream(
              path + "/docLengths.txt"));
      docLengthsMap = (HashMap<Integer, Integer>) ois4.readObject();
      System.out.println("doc lengths map size " + docLengthsMap.size());
      ois4.close();
    } catch (Exception e) {
      System.out.println("problem in getting general data");
      e.printStackTrace();
    }
  }

  public void makeTermsQueryable(ArrayList<String> terms) {
    tfScores = new HashMap<>();
    dfScores = new HashMap<>();
    tfScores_agg = new HashMap<>();
    for (String term : terms) {
      if (!catalog.containsKey(term)) {
        System.out.println("doesn't contain term: " + term);
        continue;
      }
      CatalogEntry entry = catalog.get(term);
      int l = entry.endPlace - entry.startPlace;
      byte[] bytes = new byte[l];
      try {
        invListReader.seek(entry.startPlace);
        invListReader.read(bytes, 0, l);
      } catch (IOException e) { e.printStackTrace(); }
      String invListLine = new String(bytes);
      String[] split = invListLine.split(";");
      dfScores.put(term, (double) split.length);
      double total = 0;
      double tf;
      tfScores.put(term, new HashMap<>());
      for (String s : split) {
        String[] split2 = s.split("\\|", 3);
        tf = (double) Integer.parseInt(split2[1]);
        tfScores.get(term).put(split2[0], tf);
        total += tf;
      }
      tfScores_agg.put(term, total);
    }
  }

  public double tf(String docId, String term) {
    if (!tfScores.containsKey(term)) {
      return 0;
    }
    String docKey = String.valueOf(docHashes.get(docId));
    if (!tfScores.get(term).containsKey(docKey)) {
      return 0;
    }
    return tfScores.get(term).get(docKey);
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
      dfScores.put(term, (double) 0);
      return 0;
    }
    return dfScores.get(term);
  }

  public double docLen(String document) {
    int docHash = docHashes.get(document);
    if (!docLengthsMap.containsKey(docHash)) {
      docLengthsMap.put(docHash, 0);
    }
    return (double) docLengthsMap.get(docHash);
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

  public HashMap<Integer, ArrayList<String>> getStemmed(HashMap<Integer, String> queries) {
    // HashMap<queryId, ArrayList<term>> The list of stemmed terms in each query
    HashMap<Integer, ArrayList<String>> stemmed = new HashMap<>();
    for (int i : queries.keySet()) {
      ArrayList<TermPosition> tps = tokenized.tokenize(queries.get(i), i, true);
      ArrayList<String> terms = new ArrayList<>();
      for (TermPosition tp : tps) {
        terms.add("" + tp.getTermHash());
      }
      stemmed.put(i, terms);
    }
    return stemmed;
  }
}
