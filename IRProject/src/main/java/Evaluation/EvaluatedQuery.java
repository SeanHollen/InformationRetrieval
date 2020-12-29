package Evaluation;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class EvaluatedQuery {

  private static int[] kScores = new int[]{5, 10, 20, 50, 100};
  private HashSet<Integer> kScoresHash = new HashSet<>(Arrays.asList(5, 10, 20, 50, 100));
  private static final double[] recallCheckPoints = new double[]
          {0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};

  private HashMap<Double, Double> precisionByRecall;

  private double trueRelevance;
  private int numRelevantRet;
  private double rPrecision;
  private double averagePrecision;
  private double sumPrecision;
  private double DCG;
  private double IDCG;
  private double[] precisionList;
  private double[] recallList;
  private double[] F1Scores;
  private double[] NDCGList;
  private int n;
  private int index;

  EvaluatedQuery(double trueRelevance) {
    numRelevantRet = 0;
    rPrecision = 0;
    sumPrecision = 0;
    DCG = 0;
    IDCG = 0;
    precisionList = new double[1000];
    recallList = new double[1000];
    F1Scores = new double[200];
    NDCGList = new double[200];
    n = 1;
    index = 0;
    this.trueRelevance = trueRelevance;
  }

  public void evaluateDocument(String document, LinkedList<Integer> idealScores,
                               HashMap<String, Integer> qrel) {
    double score = 0;
    if (qrel.containsKey(document)) {
      score = qrel.get(document);
    }
    if (score > 0) {
      numRelevantRet++;
      sumPrecision += (double) numRelevantRet / (double) n;
    }
    precisionList[index] = (double) numRelevantRet / (double) n;
    if (trueRelevance == 0) {
      recallList[index] = 1;
    } else {
      recallList[index] = (double) numRelevantRet / trueRelevance;
    }
    if (n == 1) {
      DCG += score;
      if (!idealScores.isEmpty()) {
        IDCG += idealScores.poll();
      }
    } else {
      DCG += score / (Math.log(n) / Math.log(2));
      if (!idealScores.isEmpty()) {
        IDCG += idealScores.poll() / (Math.log(n) / Math.log(2));
      }
    }
    if (rPrecision == 0 && recallList[index] >= precisionList[index]) {
      rPrecision = precisionList[index];
    }
    if (kScoresHash.contains(index)) {
      F1Scores[index] = F1(recallList[index], precisionList[index]);
      NDCGList[index] = DCG / IDCG;
    }
    n++;
    index++;
  }

  public void combineData(EvaluatedQuery other) {
    index += other.index;
    numRelevantRet += other.numRelevantRet;
    averagePrecision += other.averagePrecision;
    for (int i = 0; i < other.precisionList.length; i++) {
      precisionList[i] += other.precisionList[i];
    }
    rPrecision += other.rPrecision;
    for (double d : other.precisionByRecall.keySet()) {
      if (!precisionByRecall.containsKey(d)) {
        precisionByRecall.put(d, other.precisionByRecall.get(d));
      } else {
        precisionByRecall.put(d, precisionByRecall.get(d) + other.precisionByRecall.get(d));
      }
    }
    for (int i : kScores) {
      recallList[i] += other.recallList[i];
      NDCGList[i] += other.NDCGList[i];
      F1Scores[i] += other.F1Scores[i];
      kScoresHash.remove(i);
    }
  }

  public void averageByDividingBySize(double numQueries) {
    averagePrecision /= numQueries;
    for (int i = 0; i < precisionList.length; i++) {
      precisionList[i] /= numQueries;
    }
    rPrecision /= numQueries;
    for (Double d : precisionByRecall.keySet()) {
      precisionByRecall.put(d, precisionByRecall.get(d) / numQueries);
    }
    for (int i : kScores) {
      recallList[i] /= numQueries;
      NDCGList[i] /= numQueries;
      F1Scores[i] /= numQueries;
    }
  }

  public void calculatePrecisionByRecall() {
    averagePrecision = sumPrecision / trueRelevance;
    precisionByRecall = new HashMap<>();
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
  }

  private static double F1(double recall, double precision, int k) {
    int k2 = k*k;
    return (double) (k2 + 1) * recall * precision / (recall + (double) k2 * precision);
  }

  private static double F1(double recall, double precision) {
    return F1(recall, precision, 1);
  }

  public void printEval(PrintStream out, int qNum, boolean isSummary) {
    if (isSummary) {
      out.println("number of queries:  " + qNum);
    } else {
      out.println("Queryid (Num):      " + qNum);
    }
    out.println("Total number of documents over all queries");
    // Retrieved
    out.println("\tRetrieved: " + index);
    // True Relevant
    out.println("\tRelevant: " + trueRelevance);
    // Retrieved Relevant
    out.println("\tRel_ret: " + numRelevantRet);
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
    out.println("\n                  " + round(averagePrecision));
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
