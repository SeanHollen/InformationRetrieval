package MachineLearning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class DocManager {

  private HashSet<Integer> allQueries;
  private HashSet<Integer> testingQueries;
  private HashSet<Integer> trainingQueries;

  private final String rankingResults = "out/RankingResults";

  public DocManager(ArrayList<Integer> queryIds, int numForTesting) {
    allQueries = new HashSet<>(queryIds);
    Collections.shuffle(queryIds);
    testingQueries = new HashSet<>(queryIds.subList(0, numForTesting - 1));
    trainingQueries = new HashSet<>(queryIds.subList(numForTesting, queryIds.size()));
  }

  public void collectRankingResults(String qrelFile, String testingOut, String trainingOut)
          throws IOException {
    // Get sets of results
    File[] resultsFiles = new File(rankingResults).listFiles();
    // HashMap<qid-docid (table row), HashMap<f1 f2 f3 … fd label (table columns), values>
    HashMap<String, HashMap<String, String>> trainingTable = new HashMap<>();
    HashMap<String, HashMap<String, String>> testingTable = new HashMap<>();
    ArrayList<String> calculationTypes = new ArrayList<>();
    for (File nextFile : resultsFiles) {
      calculationTypes.add(nextFile.getName());
      BufferedReader reader = new BufferedReader(new FileReader(nextFile));
      String line;
      while ((line = reader.readLine()) != null) {
        String[] splitLine = line.split(" +");
        String queryNum = splitLine[0];
        String docId = splitLine[1];
        String queryId_docId = queryNum + "_" + docId;
        if (trainingQueries.contains(Integer.parseInt(queryNum))) {
          trainingTable.putIfAbsent(queryId_docId, new HashMap<>());
          trainingTable.get(queryId_docId).put(nextFile.getName(), splitLine[4]);
        } else if (testingQueries.contains(Integer.parseInt(queryNum))) {
          testingTable.putIfAbsent(queryId_docId, new HashMap<>());
          testingTable.get(queryId_docId).put(nextFile.getName(), splitLine[4]);
        } else {
          throw new IllegalArgumentException("query ID in results file not found in queryIds argument");
        }
      }
    }
    // qrel read
    BufferedReader qrelFileReader = new BufferedReader(new FileReader(new File(qrelFile)));
    // HashMap<qid-docid, Score>
    HashMap<String, Integer> trueScores = new HashMap<>();
    String line;
    while ((line = qrelFileReader.readLine()) != null) {
      String[] split = line.split(" ");
      String queryId = split[0];
      String docId = split[2];
      String queryId_docId = queryId + "_" + docId;
      int score = Integer.parseInt(split[3]);
      trueScores.put(queryId_docId, score);
    }
    // print testing
    PrintWriter testingWriter = new PrintWriter(new FileWriter(testingOut));
    for (String queryId_docId : testingTable.keySet()) {
      testingWriter.print(queryId_docId);
      for (String type : calculationTypes) {
        testingWriter.print(testingTable.get(type));
      }
      testingWriter.println(trueScores.get(queryId_docId));
    }
    // print training
    PrintWriter trainingWriter = new PrintWriter(new FileWriter(trainingOut));
    for (String queryId_docId : trainingTable.keySet()) {
      trainingWriter.print(queryId_docId);
      for (String type : calculationTypes) {
        trainingWriter.print(trainingTable.get(type));
      }
      trainingWriter.println(trueScores.get(queryId_docId));
    }
  }

  public HashSet<Integer> getAllQueries() {
    return allQueries;
  }

  public void segregateQueries() {
    // todo
  }

  public HashSet<Integer> getTrainingQueries() {
    return trainingQueries;
  }

  public HashSet<Integer> getTestingQueries() {
    return testingQueries;
  }

  public HashMap<Integer, PriorityQueue<DocScore>> getQRELMap() {
    // todo
    return new HashMap<>();
  }
}
