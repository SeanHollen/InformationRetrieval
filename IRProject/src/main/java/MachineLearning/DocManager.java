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

public class DocManager {

  private HashSet<Integer> allQueries;
  private HashSet<Integer> testingQueries;
  private HashSet<Integer> trainingQueries;
  private HashMap<String, Integer> qrelMap;

  public DocManager(ArrayList<Integer> queryIds, int numForTesting) {
    allQueries = new HashSet<>(queryIds);
    Collections.shuffle(queryIds);
    testingQueries = new HashSet<>(queryIds.subList(0, numForTesting));
    trainingQueries = new HashSet<>(queryIds.subList(numForTesting, queryIds.size()));
    System.out.println(allQueries.size());
    System.out.println(testingQueries.size());
    System.out.println(trainingQueries.size());
  }

  public void writeMatrixFiles(String outPath, String rankingResultsPath)
          throws IOException {
    // Get sets of results
    File[] resultsFiles = new File(rankingResultsPath).listFiles();
    // HashMap<qid-docid (table row), HashMap<f1 f2 f3 … fd label (table columns), values>
    HashMap<String, HashMap<String, String>> table = new HashMap<>();
    ArrayList<String> calculationTypes = new ArrayList<>();
    for (File aFile : resultsFiles) {
      calculationTypes.add(aFile.getName());
      BufferedReader reader = new BufferedReader(new FileReader(aFile));
      String line;
      System.out.println("reading file " + aFile.getName());
      while ((line = reader.readLine()) != null) {
        String[] splitLine = line.split(" +");
        String queryNum = splitLine[0];
        String docId = splitLine[2];
        String queryId_docId = queryNum + "_" + docId;
        if (allQueries.contains(Integer.parseInt(queryNum))) {
          table.putIfAbsent(queryId_docId, new HashMap<>());
          table.get(queryId_docId).put(aFile.getName(), splitLine[4]);
        } else {
          throw new IllegalArgumentException("query ID in results file " + queryNum
                  + " not found in queryIds argument");
        }
      }
    }
    System.out.println(table);
    // print
    PrintWriter trainingWriter = new PrintWriter(new FileWriter(outPath));
    trainingWriter.println("@RELATION ML");
    trainingWriter.println("@ATTRIBUTE ES NUMERIC");
    trainingWriter.println("@ATTRIBUTE OKAPI_TF NUMERIC");
    trainingWriter.println("@ATTRIBUTE TF_IDF NUMERIC");
    trainingWriter.println("@ATTRIBUTE OKAPI_BM25 NUMERIC");
    trainingWriter.println("@ATTRIBUTE LM_LAPLACE NUMERIC");
    trainingWriter.println("@ATTRIBUTE LM_JM NUMERIC");
    trainingWriter.println("@ATTRIBUTE label NUMERIC");
    trainingWriter.println("@DATA");
    for (String queryId_docId : table.keySet()) {
      if (!qrelMap.containsKey(queryId_docId)) {
        continue;
      }
      trainingWriter.print(queryId_docId + " ");
      for (String type : calculationTypes) {
        trainingWriter.print(table.get(queryId_docId).get(type) + " ");
      }
      trainingWriter.println(qrelMap.get(queryId_docId));
    }
    trainingWriter.flush();
  }

  public void generateQrelMap(String qrelFile) throws IOException {
    // qrel read
    BufferedReader qrelFileReader = new BufferedReader(new FileReader(new File(qrelFile)));
    // HashMap<qid-docid, Score>
    qrelMap = new HashMap<>();
    String line;
    while ((line = qrelFileReader.readLine()) != null) {
      String[] split = line.split(" ");
      String queryId = split[0];
      String docId = split[2];
      String queryId_docId = queryId + "_" + docId;
      int score = Integer.parseInt(split[3]);
      qrelMap.put(queryId_docId, score);
    }
  }

  public HashSet<Integer> getAllQueries() {
    return allQueries;
  }

  public HashSet<Integer> getTrainingQueries() {
    return trainingQueries;
  }

  public HashSet<Integer> getTestingQueries() {
    return testingQueries;
  }

  public HashMap<String, Integer> getQrelMap() {
    return qrelMap;
  }
}
