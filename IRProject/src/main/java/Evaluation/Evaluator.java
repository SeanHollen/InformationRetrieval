package Evaluation;

import java.io.*;
import java.util.*;

public class Evaluator {

  private static int[] kScores = new int[]{5, 10, 20, 50, 100};
  private HashSet<Integer> kScoresHash = new HashSet<>(Arrays.asList(5, 10, 20, 50, 100));
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
    // *** read from files ***
    BufferedReader qrelFileReader = new BufferedReader(new FileReader(new File(qrelFile)));
    // HashMap<QueryId, HashMap<DocId, Score>>
    HashMap<Integer, HashMap<String, Integer>> qrel = new HashMap<>();
    String line;
    String[] split;
    while ((line = qrelFileReader.readLine()) != null) {
      split = line.split(" ");
      int queryId = Integer.parseInt(split[0]);
      qrel.putIfAbsent(queryId, new HashMap<>());
      qrel.get(queryId).put(split[2], Integer.parseInt(split[3]));
    }
    // HashMap<QueryId, ordered ArrayList<DocId>>
    HashMap<Integer, ArrayList<String>> results = new HashMap<>();
    BufferedReader resultsFileReader = new BufferedReader(new FileReader(new File(resultsFile)));
    while ((line = resultsFileReader.readLine()) != null) {
      split = line.split(" +");
      int queryId = Integer.parseInt(split[0]);
      results.putIfAbsent(queryId, new ArrayList<>());
      results.get(queryId).add(split[2]);
    }
    // *** calculate ideal scores ***
    HashMap<Integer, LinkedList<Integer>> idealScores = new HashMap<>();
    HashMap<Integer, Integer> trueRelevance = new HashMap<>();
    int metaTrueRelevant = 0;
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
      if (countRelevant > 1000) {
        countRelevant = 1000;
      }
      trueRelevance.put(key, countRelevant);
      metaTrueRelevant += countRelevant;
      idealScores.put(key, values);
    }
    // *** initialize sum data ***
    int metaSumRetrieved = 0;
    int metaNumRelevant = 0;
    double metaAvgPrecision = 0;
    double[] metaPrecisionList = new double[1000];
    double metaRPrecision = 0;
    double[] metaRecallList = new double[1000];
    double[] metaNDCGList = new double[1000];
    double[] metaF1Scores = new double[1000];
    HashMap<Double, Double> metaPrecisionByRecall = new HashMap<>();
    // *** get data ***
    for (int queryId : results.keySet()) {
      System.out.println(results.get(queryId));
      int numRelevantRet = 0;
      double rPrecision = 0;
      double sumPrecision = 0;
      double DCG = 0;
      double IDCG = 0;
      double[] precisionList = new double[1000];
      double[] recallList = new double[1000];
      double[] NDCGList = new double[1000];
      double[] F1Scores = new double[1000];
      int n = 1;
      int index = 0;
      for (String document : results.get(queryId)) {
        double score = 0;
        if (qrel.get(queryId).containsKey(document)) {
          score = qrel.get(queryId).get(document);
        }
        if (score > 0) {
          numRelevantRet++;
        }
        precisionList[index] = numRelevantRet / (double) n;
        sumPrecision += precisionList[index];
        if (trueRelevance.get(queryId) == 0) {
          recallList[index] = 1;
        } else {
          recallList[index] = (double) numRelevantRet / (double) trueRelevance.get(queryId);
        }
        if (n == 1) {
          DCG += score;
          if (!idealScores.get(queryId).isEmpty()) {
            IDCG += idealScores.get(queryId).poll();
          }
        } else {
          DCG += score / (Math.log(n) / Math.log(2));
          if (!idealScores.get(queryId).isEmpty()) {
            IDCG += idealScores.get(queryId).poll() / (Math.log(n) / Math.log(2));
          }
        }
        if (rPrecision == 0 && recallList[index] >= precisionList[index]) {
          rPrecision = precisionList[index];
        }
        if (kScoresHash.contains(index)) {
          F1Scores[index] = F1(recallList[index], precisionList[index]);
          NDCGList[index] = DCG / IDCG;
        }
//        System.out.println(document + "\t" + score + "\t" + round(precisionList[index]) + "\t" + round(recallList[index]));
        n++;
        index++;
      }
      double averagePrecision = sumPrecision / (double) n;
      // *** calculates precision by recall ***
      HashMap<Double, Double> precisionByRecall = new HashMap<>();
      int x = 0;
      for (double checkPoint : recallCheckPoints) {
        while (x < recallList.length && recallList[x] < checkPoint) {
          x++;
          if (x >= recallList.length) { break; }
        }
        if (x >= recallList.length) {
          precisionByRecall.put(checkPoint, 0.0);
        } else {
          precisionByRecall.put(checkPoint, precisionList[x]);
        }
      }
      // *** prints ***
      if (toExpound) {
        printEval(queryId, n, trueRelevance.get(queryId), numRelevantRet, averagePrecision,
                precisionList, rPrecision, precisionByRecall, NDCGList, F1Scores, recallList,
                false);
      }
      // *** combine data ***
      metaSumRetrieved += n;
      metaNumRelevant += numRelevantRet;
      metaAvgPrecision += averagePrecision;
      for (int i = 0; i < precisionList.length; i++) {
        metaPrecisionList[i] += precisionList[i];
      }
      metaRPrecision += rPrecision;
      for (double d : precisionByRecall.keySet()) {
        if (!metaPrecisionByRecall.containsKey(d)) {
          metaPrecisionByRecall.put(d, precisionByRecall.get(d));
        } else {
          metaPrecisionByRecall.put(d, metaPrecisionByRecall.get(d) + precisionByRecall.get(d));
        }
      }
      for (int i : kScores) {
        metaRecallList[i] += recallList[i];
        metaNDCGList[i] += NDCGList[i];
        metaF1Scores[i] += F1Scores[i];
        kScoresHash.remove(i);
      }
    }
    // *** averages by dividing by size ***
    double size = results.size();
    metaAvgPrecision /= size;
    for (int i = 0; i < metaPrecisionList.length; i++) {
      metaPrecisionList[i] /= size;
    }
    metaRPrecision /= size;
    for (Double d : metaPrecisionByRecall.keySet()) {
      metaPrecisionByRecall.put(d, metaPrecisionByRecall.get(d) / size);
    }
    for (int i = 0; i < metaRecallList.length; i++) { // todo
      metaRecallList[i] /= size;
    }
    for (int i = 0; i < metaNDCGList.length; i++) {
      metaNDCGList[i] /= size;
    }
    for (int i = 0; i < metaF1Scores.length; i++) {
      metaF1Scores[i] /= size;
    }
    // *** prints ***
    printEval(results.size(), metaSumRetrieved, metaTrueRelevant, metaNumRelevant, metaAvgPrecision,
            metaPrecisionList, metaRPrecision, metaPrecisionByRecall, metaNDCGList, metaF1Scores,
            metaRecallList,true);
  }

  private static double F1(double recall, double precision, int k) {
    int k2 = k*k;
    return (double) (k2 + 1) * recall * precision / (recall + (double) k2 * precision);
  }

  private static double F1(double recall, double precision) {
    return F1(recall, precision, 1);
  }

  private void printEval(
          int qNum, int totalRetieved, int trueRelevant, int relevantRetrived, double avgPrecision,
          double[] precisionList, double rPrecision, HashMap<Double, Double> precisionByRecall,
          double[] NDCGList, double[] F1Scores, double[] recallList, boolean isSummary) {
    if (isSummary) {
      out.println("number of queries:  " + qNum);
    } else {
      out.println("Queryid (Num):      " + qNum);
    }
    out.println("Total number of documents over all queries");
    // Retrieved
    out.println("\tRetrieved: " + totalRetieved);
    // True Relevant
    out.println("\tRelevant: " + trueRelevant);
    // Retrieved Relevant
    out.println("\tRel_ret: " + relevantRetrived);
    out.println("Interpolated Recall - Precision Averages:");
    for (double i : recallCheckPoints) {
      // Precision @ Recall levels
      out.println("\tAt " + i + "       " + round(precisionByRecall.get(i)));
    }
    // Average Precision
    out.print("Average precision (non-interpolated) for all rel docs");
    if (isSummary) {
      out.print("(all stats averaged over queries)");
    }
    out.println("\n                  " + round(avgPrecision));
    out.println("Precision:");
    for (int i : new int[]{5, 10, 15, 20, 30, 100, 200, 500, 1000}) {
      out.print("\tAt" + "     ".substring(0, 5 - String.valueOf(i).length()));
      // Precision @k
      out.println(i + " docs:   " + round(precisionList[i - 1]));
    }
    out.println("R-Precision (precision after R (= num_rel for a query) docs retrieved):");
    // R-Precision
    out.println("\tExact:        " + round(rPrecision));

    out.println("Recall:");
    for (int i = 0; i < kScores.length; i++) {
      // Recall
      int docPosn = kScores[i];
      out.print("\tAt" + "     ".substring(0, 5 - String.valueOf(docPosn).length()));
      out.println(docPosn + " docs:   " + round(NDCGList[docPosn]));
    }
    out.println("F1 Scores:");
    for (int i = 0; i < kScores.length; i++) {
      // F1 scores
      int docPosn = kScores[i];
      out.print("\tAt" + "     ".substring(0, 5 - String.valueOf(docPosn).length()));
      out.println(docPosn + " docs:   " + round(F1Scores[docPosn]));
    }
    out.println("nDCG scores");
    for (int i = 0; i < kScores.length; i++) {
      // nDCG
      int docPosn = kScores[i];
      out.print("\tAt" + "     ".substring(0, 5 - String.valueOf(docPosn).length()));
      out.println(docPosn + " docs:   " + round(recallList[docPosn]));
    }
    System.out.print("\n");
  }

  private static String round(double x) {
    return String.format("%.4f", x);
  }
}
