package Evaluation;

import java.io.*;
import java.util.*;

public class Evaluator {

  private static final HashSet<Integer> kScores = new HashSet<>(
          Arrays.asList(5, 10, 20, 50, 100));
  private static final double[] recallCheckPoints = new double[]
          {0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
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

  // qrelFile: manually ranked documents for use as reference guide
  // resultsFile: calculated document scores from ranking by algorithm
  public void evaluate(String qrelFile, String resultsFile, boolean toExpound) throws IOException {
    BufferedReader qrelFileReader = new BufferedReader(new FileReader(new File(qrelFile)));
    // HashMap<QueryId, HashMap<DocId, Score>>
    HashMap<Integer, HashMap<String, Integer>> qrel = new HashMap<>();
    String line;
    String[] split;
    while ((line = qrelFileReader.readLine()) != null) {
      split = line.split(" ");
      int queryId = Integer.parseInt(split[0]);
      if (!qrel.containsKey(queryId)) {
        qrel.put(queryId, new HashMap<>());
      }
      qrel.get(queryId).put(split[2], Integer.parseInt(split[3]));
    }
    // HashMap<QueryId, ordered ArrayList<DocId>>
    HashMap<Integer, ArrayList<String>> results = new HashMap<>();
    BufferedReader resultsFileReader = new BufferedReader(new FileReader(new File(resultsFile)));
    while ((line = resultsFileReader.readLine()) != null) {
      split = line.split(" +");
      int queryId = Integer.parseInt(split[0]);
      if (!results.containsKey(queryId)) {
        results.put(queryId, new ArrayList<>());
      }
      results.get(queryId).add(split[2]);
    }
    HashMap<Integer, LinkedList<Integer>> idealScores = new HashMap<>();
    HashMap<Integer, Integer> trueRelevance = new HashMap<>();
    for (Integer key : qrel.keySet()) {
      idealScores.put(key, new LinkedList<>());
      LinkedList<Integer> values = new LinkedList<>(qrel.get(key).values());
      values.sort(Collections.reverseOrder());
      int countRelevant = 0;
      for (int val : values) {
        if (val > 0) {
          countRelevant++;
        } else {
          break;
        }
      }
      trueRelevance.put(key, countRelevant);
      idealScores.put(key, values);
    }
    // initializing sum data
    int sumNumRelevant = 0;
    double sumAvgPrecision = 0;
    double[] sumPrecisionList = new double[1000];
    double sumRPrecision = 0;
    HashMap<Double, Double> sumPrecisionByRecall = new HashMap<>();
    int sumRetrieved = 0;
    double sumTrueRelevance = 0;
    // get data
    for (int queryId : results.keySet()) {
      System.out.println("next query id " + queryId);
      int numRelevant = 0;
      double rPrecision = 0;
      double sumPrecision = 0;
      double DCG = 0;
      double IDCG = 0;
      double[] precisionList = new double[1000];
      double[] recallList = new double[1000];
      double[] NDCGList = new double[1000];
      ArrayList<Double> F1Scores = new ArrayList<>();
      int x = 0;
      for (int i = 1; i <= results.get(queryId).size(); i++) {
        String document = results.get(queryId).get(i - 1);
        if (!qrel.get(queryId).containsKey(document)) {
          continue;
        }
        x++;
        double score = qrel.get(queryId).get(document);
        // todo is score > 0 correct?
        if (score > 0) {
          numRelevant++;
        }
        double precision = numRelevant / (double) x;
        precisionList[x - 1] = precision;
        sumPrecision += precision;
        double recall = 1;
        if (trueRelevance.get(queryId) != 0) {
          recall = (double) numRelevant / (double) trueRelevance.get(queryId);
        }
        recallList[x - 1] = recall;
        if (i == 1) {
          DCG += score;
          IDCG += idealScores.get(queryId).poll();
        } else {
          DCG += score / (Math.log(i) / Math.log(2));
          IDCG += idealScores.get(queryId).poll() / (Math.log(i) / Math.log(2));
        }
        NDCGList[x - 1] = DCG / IDCG;
        if (rPrecision == 0 && recall >= precision) {
          rPrecision = precision;
        }
        if (kScores.contains(i)) {
          F1Scores.add(F1(recall, precision));
        }
      }
      double averagePrecision = sumPrecision / (double) x;
      HashMap<Double, Double> precisionByRecall = new HashMap<>();
      int i = 0;
      for (double checkPoint : recallCheckPoints) {
        while (i < recallList.length && recallList[i] < checkPoint) {
          i++;
          if (i >= recallList.length) { break; }
        }
        if (i >= recallList.length) {
          precisionByRecall.put(checkPoint, 0.0);
        } else {
          precisionByRecall.put(checkPoint, precisionList[i]);
        }
      }
      System.out.println(Arrays.toString(recallList));
      System.out.println(Arrays.toString(precisionList));
      if (toExpound) {
        printResults(queryId, results.get(queryId).size(), trueRelevance.get(queryId), numRelevant,
                averagePrecision, precisionList, rPrecision, precisionByRecall);
      }
      // combine data
      sumNumRelevant += numRelevant;
      sumAvgPrecision += averagePrecision;
      for (int n = 0; n < precisionList.length; n++) {
        sumPrecisionList[n] += precisionList[n];
      }
      sumRPrecision += rPrecision;
      for (double d : precisionByRecall.keySet()) {
        if (!sumPrecisionByRecall.containsKey(d)) {
          sumPrecisionByRecall.put(d, precisionByRecall.get(d));
        } else {
          sumPrecisionByRecall.put(d, sumPrecisionByRecall.get(d) + precisionByRecall.get(d));
        }
      }
      sumRetrieved += results.get(queryId).size();
      sumTrueRelevance += trueRelevance.get(queryId);
    }
    int size = results.size();
    sumRetrieved /= size;
    sumTrueRelevance /= size;
    sumNumRelevant /= size;
    sumAvgPrecision /= size;
    for (int i = 0; i < sumPrecisionList.length; i++) {
      sumPrecisionList[i] /= size;
    }
    sumRetrieved /= size;
    for (Double d : sumPrecisionByRecall.keySet()) {
      sumPrecisionByRecall.put(d, sumPrecisionByRecall.get(d) / size);
    }

    printResults(results.size(), sumRetrieved, sumTrueRelevance, sumNumRelevant, sumAvgPrecision,
            sumPrecisionList, sumRPrecision, sumPrecisionByRecall);
  }

  private static double F1(double recall, double precision, int k) {
    int k2 = k*k;
    return (double) (k2 + 1) * recall * precision / (recall + (double) k2 * precision);
  }

  private static double F1(double recall, double precision) {
    return F1(recall, precision, 1);
  }

  private void printResults(
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
      out.println("\tAt " + i + "       " + precisionByRecall.get(i));
    }
    // Average Precision
    out.println("Average precision (non-interpolated) for all rel docs(averaged over queries)");
    out.println("Precision:");
    for (int i : new int[]{5, 10, 15, 20, 30, 100, 200, 500, 1000}) {
      out.print("\tAt" + "     ".substring(0, 5 - String.valueOf(i).length()));
      // Precision @k
      out.println(i + " docs:   " + precisionList[i - 1]);
    }
    out.println("R-Precision (precision after R (= num_rel for a query) docs retrieved):");
    // R-Precision
    out.println("\tExact:        " + rPrecision + "\n");
  }
}
