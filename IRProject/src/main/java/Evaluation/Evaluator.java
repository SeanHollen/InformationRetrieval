package Evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class Evaluator {

  private final HashSet<Integer> kScores = new HashSet<Integer>(Arrays.asList(5, 10, 20, 50, 100));
  private PrintStream out;

  public Evaluator() {
    out = System.out;
  }

  public Evaluator(String out) {
    try {
      this.out = new PrintStream(out);
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public void evaluate(String qrelFile, String resultsFile) throws IOException {
    BufferedReader qrelFileReader = new BufferedReader(new FileReader(new File(qrelFile)));
    // HashMap<QueryId, HashMap<DocId, Score>>
    HashMap<Integer, HashMap<String, Integer>> qrel = new HashMap<Integer, HashMap<String, Integer>>();
    String line;
    String[] split;
    while ((line = qrelFileReader.readLine()) != null) {
      split = line.split(" ");
      int queryId = Integer.parseInt(split[0]);
      if (!qrel.containsKey(queryId)) {
        qrel.put(queryId, new HashMap<String, Integer>());
      }
      qrel.get(queryId).put(split[2], Integer.parseInt(split[3]));
    }
    // HashMap<QueryId, ordered ArrayList<DocId>>
    HashMap<Integer, ArrayList<String>> results = new HashMap<Integer, ArrayList<String>>();
    BufferedReader resultsFileReader = new BufferedReader(new FileReader(new File(resultsFile)));
    while ((line = resultsFileReader.readLine()) != null) {
      split = line.split(" ");
      int queryId = Integer.parseInt(split[0]);
      if (!results.containsKey(queryId)) {
        results.put(queryId, new ArrayList<String>());
      }
      results.get(queryId).add(split[3]);
    }
    HashMap<Integer, ArrayList<Integer>> IDCGscores = new HashMap<Integer, ArrayList<Integer>>();
    for (Integer key : qrel.keySet()) {
      IDCGscores.put(key, new ArrayList<Integer>());
      ArrayList<Integer> values = new ArrayList<Integer>(qrel.get(key).values());
      Collections.sort(values);
      IDCGscores.put(key, values);
    }
    double precision;
    double recall;
    double rPrecision = -1;
    double sumPrecision = 0;
    double averagePrecision;
    double f1;
    double IDCG = 0;
    double DCG = 0;
    for (int queryId : results.keySet()) {
      for (int i = 0; i < results.get(queryId).size(); i++) {
        String document = results.get(queryId).get(i);
        precision = 0; // todo
        recall = 0; // todo
        sumPrecision += precision;
        if (i == 0) {
          DCG += qrel.get(queryId).get(document);
        } else {
          DCG += qrel.get(queryId).get(document) / (Math.log(i) / Math.log(2));
        }
        IDCG += IDCGscores.get(queryId).get(i); 
        if (rPrecision == -1 && recall >= precision) {
          rPrecision = precision;
        }
        if (kScores.contains(i)) {
          f1 = F1(recall, precision, i);
          averagePrecision = sumPrecision / sumPrecision;
        }
      }
    }
  }

  public static double F1(double recall, double precision, int k) {
    // (b^2+1) * recall*precision / (recall + b^2 * precision)
    int k2 = k*k;
    return (double) (k2 + 1) * recall * precision / (recall + (double) k2 * precision);
  }
}
