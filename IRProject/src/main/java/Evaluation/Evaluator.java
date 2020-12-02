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

  private static final HashSet<Integer> kScores = new HashSet<Integer>(Arrays.asList(5, 10, 20, 50, 100));
  private static final double[] recallCheckPoints = new double[]{0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
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
      split = line.split(" +");
      int queryId = Integer.parseInt(split[0]);
      if (!results.containsKey(queryId)) {
        results.put(queryId, new ArrayList<String>());
      }
      results.get(queryId).add(split[3]);
    }
    HashMap<Integer, ArrayList<Integer>> IDCGscores = new HashMap<Integer, ArrayList<Integer>>();
    HashMap<Integer, Integer> trueRelevance = new HashMap<Integer, Integer>();
    for (Integer key : qrel.keySet()) {
      IDCGscores.put(key, new ArrayList<Integer>());
      ArrayList<Integer> values = new ArrayList<Integer>(qrel.get(key).values());
      Collections.sort(values);
      int relevant = 0;
      for (int val : values) {
        if (val > 0) {
          relevant++;
        } else {
          break;
        }
      }
      trueRelevance.put(key, relevant);
      IDCGscores.put(key, values);
    }
    for (int queryId : results.keySet()) {
      int numRelevant = 0;
      double rPrecision = -1;
      double sumPrecision = 0;
      double DCG = 0;
      double IDCG = 0;
      double averagePrecision = 0;
      double[] precisionList = new double[1000];
      double[] recallList = new double[1000];
      double[] NDCGList = new double[1000];
      double[] F1Scores = new double[kScores.size()];
      for (int i = 0; i < results.get(queryId).size(); i++) {
        String document = results.get(queryId).get(i);
        int score = qrel.get(queryId).get(document);
        if (score > 0) { //todo correct?
          numRelevant++;
        }
        double precision = numRelevant / (i + 1);
        precisionList[i] = precision;
        sumPrecision += precision;
        double recall = numRelevant / trueRelevance.get(queryId);
        recallList[i] = recall;
        if (i == 0) {
          DCG += score;
        } else {
          DCG += score / (Math.log(i) / Math.log(2));
        }
        IDCG += IDCGscores.get(queryId).get(i);
        NDCGList[i] = DCG / IDCG;
        if (rPrecision == -1 && recall >= precision) {
          rPrecision = precision;
        }
        if (kScores.contains(i)) {
          F1Scores[i] = F1(recall, precision, i);
          averagePrecision = sumPrecision / sumPrecision;
        }
      }
      HashMap<Double, Double> precisionByRecall = new HashMap<Double, Double>();
      int i = 0;
      for (double r : recallCheckPoints) {
        while (recallList[i] < r) {
          i++;
          if (i == 1000) {
            precisionByRecall.put(r, 0.0);
            break;
          }
          precisionByRecall.put(r, precisionList[i]);
        }
      }
      printStuff(queryId, results.get(queryId).size(), trueRelevance.get(queryId), numRelevant,
              averagePrecision, precisionList, rPrecision, precisionByRecall);
    }

  }

  private static double F1(double recall, double precision, int k) {
    // (b^2+1) * recall*precision / (recall + b^2 * precision)
    int k2 = k*k;
    return (double) (k2 + 1) * recall * precision / (recall + (double) k2 * precision);
  }

  private void printStuff(
          int qNum, int retrieved, double trueRelevance, int ret_rel, double avgPrecision,
          double[] precisionList, double rPrecision, HashMap<Double, Double> precisionByRecall) {
    out.println("Queryid (Num):      " + qNum);
    out.println("Total number of documents over all queries");
    // Retrieved
    out.println("\tRetrieved: " + retrieved);
    // True Relevant
    out.println("\tRelevant: " + trueRelevance);
    // Retrieved Relevant
    out.println("\tRel_ret: " + ret_rel);
    out.println("Interpolated Recall - Precision Averages:");
    out.println("                  " + avgPrecision);
    for (double i : recallCheckPoints) {
      // Precision @ Recall levels
      System.out.println("\tAt " + i + "       " + precisionByRecall.get(i));
    }
    // Average Precision
    out.println("Average precision (non-interpolated) for all rel docs(averaged over queries)");
    out.println("Precision:");
    for (int i : new int[]{5, 10, 15, 20, 30, 100, 200, 500, 1000}) {
      out.print("\tAt" + "     ".substring(0, 5 - String.valueOf(i).length()));
      // Precision @k
      out.println(i + " docs:   " + precisionList[i]);
    }
    out.println("R-Precision (precision after R (= num_rel for a query) docs retrieved):");
    // R-Precision
    out.println("\tExact:        " + rPrecision);
  }
}
