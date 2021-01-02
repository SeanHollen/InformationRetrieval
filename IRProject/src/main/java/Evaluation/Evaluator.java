package Evaluation;

import java.io.*;
import java.util.*;

public class Evaluator {

  private HashMap<Integer, HashMap<String, Integer>> qrel;
  private HashMap<Integer, LinkedList<Integer>> idealScores;
  private HashMap<Integer, Integer> trueRelevance;
  private HashMap<Integer, ArrayList<String>> results;
  private int aggregateTrueRelevant;

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

  // @Param qrelFile      manually ranked documents for use as reference guide
  // @Param resultsFile   calculated document scores from ranking by algorithm
  public void evaluate(String qrelFile, String resultsFile) throws IOException {
    evaluateShort(qrelFile, resultsFile, true);
  }

  public void evaluateShort(String qrelFile, String resultsFile) throws IOException {
    evaluateShort(qrelFile, resultsFile, false);
  }

  private void  evaluateShort(String qrelFile, String resultsFile, boolean toExpound)
          throws IOException {
    readFromFiles(qrelFile, resultsFile);
    calculateIdealScores();
    QueryResultEval aggregateOfQueries = new QueryResultEval(aggregateTrueRelevant);
    for (int queryId : results.keySet()) {
      QueryResultEval evaluatedQuery = new QueryResultEval(trueRelevance.get(queryId));
      for (String document : results.get(queryId)) {
        evaluatedQuery.evaluateDocument(document, idealScores.get(queryId), qrel.get(queryId));
      }
      evaluatedQuery.calculatePrecisionByRecall();
      if (toExpound) {
        evaluatedQuery.printEval(out, queryId, false);
      }
      aggregateOfQueries.combineData(evaluatedQuery);
    }
    aggregateOfQueries.averageByDividingBySize(results.size());
    aggregateOfQueries.printEval(out, results.size(), true);
  }

  private void readFromFiles(String qrelFile, String resultsFile) throws IOException {
    BufferedReader qrelFileReader = new BufferedReader(new FileReader(new File(qrelFile)));
    // HashMap<QueryId, HashMap<DocId, Score>>
    qrel = new HashMap<>();
    String line;
    String[] split;
    while ((line = qrelFileReader.readLine()) != null) {
      split = line.split(" ");
      int queryId = Integer.parseInt(split[0]);
      qrel.putIfAbsent(queryId, new HashMap<>());
      qrel.get(queryId).put(split[2], Integer.parseInt(split[3]));
    }
    // HashMap<QueryId, ordered ArrayList<DocId>>
    results = new HashMap<>();
    BufferedReader resultsFileReader = new BufferedReader(new FileReader(new File(resultsFile)));
    while ((line = resultsFileReader.readLine()) != null) {
      split = line.split(" +");
      int queryId = Integer.parseInt(split[0]);
      results.putIfAbsent(queryId, new ArrayList<>());
      results.get(queryId).add(split[2]);
    }
  }

  private void calculateIdealScores() {
    idealScores = new HashMap<>();
    trueRelevance = new HashMap<>();
    aggregateTrueRelevant = 0;
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
      aggregateTrueRelevant += countRelevant;
      idealScores.put(key, values);
    }
  }

}
