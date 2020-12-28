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

  public void generateMatrix(String outArff, String outTxt, String rankingResultsPath)
          throws IOException {
    // Get sets of results
    File[] resultsFiles = new File(rankingResultsPath).listFiles();
    // HashMap<qid-docid (table row), HashMap<f1/f2/f3/…/fd label (table columns), values>
    HashMap<String, HashMap<String, String>> trainingTable = new HashMap<>();
    HashMap<String, HashMap<String, String>> testingTable = new HashMap<>();
    ArrayList<String> calculationTypes = new ArrayList<>();
    for (File aFile : resultsFiles) {
      String fileName = aFile.getName().replaceAll("\\W", "_").split("\\.")[0];
      calculationTypes.add(fileName);
      BufferedReader reader = new BufferedReader(new FileReader(aFile));
      String line;
      System.out.println("reading file " + aFile.getName());
      while ((line = reader.readLine()) != null) {
        String[] splitLine = line.split(" +");
        String queryNum = splitLine[0];
        String docId = splitLine[2];
        String queryId_docId = queryNum + "_" + docId;
        if (!qrelMap.containsKey(queryId_docId)) {
          continue;
        }
        if (trainingQueries.contains(Integer.parseInt(queryNum))) {
          trainingTable.putIfAbsent(queryId_docId, new HashMap<>());
          trainingTable.get(queryId_docId).put(fileName, splitLine[4]);
        } else if (testingQueries.contains(Integer.parseInt(queryNum))) {
          testingTable.putIfAbsent(queryId_docId, new HashMap<>());
          testingTable.get(queryId_docId).put(fileName, splitLine[4]);
        } else {
          throw new IllegalArgumentException("query ID in results file " + queryNum
                  + " not found in queryIds argument");
        }
      }
    }
    // print
    PrintWriter arffWriter = new PrintWriter(new FileWriter(outArff));
    PrintWriter txtWriter = new PrintWriter(new FileWriter(outTxt));
    arffWriter.println("@RELATION ML");
    for (String type : calculationTypes) {
      arffWriter.println("@ATTRIBUTE " + type.toUpperCase() + " NUMERIC");
    }
    arffWriter.println("@ATTRIBUTE label NUMERIC");
    arffWriter.println("@DATA");
    printToMatrixFiles(trainingTable, calculationTypes, arffWriter, txtWriter, false);
    printToMatrixFiles(testingTable, calculationTypes, arffWriter, txtWriter, true);
  }

  public void printToMatrixFiles(HashMap<String, HashMap<String, String>> table,
                                 ArrayList<String> calculationTypes, PrintWriter arffWriter,
                                 PrintWriter txtWriter, boolean testing) {
    // print training data
    for (String queryId_docId : table.keySet()) {
      txtWriter.print(queryId_docId + " ");
      for (String type : calculationTypes) {
        if (!table.get(queryId_docId).containsKey(type)) {
          // todo should never hapen
          arffWriter.print((double) 0 + " ");
          txtWriter.print((double) 0 + " ");
          continue;
        }
        String score = table.get(queryId_docId).get(type);
        arffWriter.print(score + " ");
        txtWriter.print(score + " ");
      }
      if (testing) {
        arffWriter.println("?");
        txtWriter.println("?");
      } else {
        arffWriter.println(qrelMap.get(queryId_docId));
        txtWriter.println(qrelMap.get(queryId_docId));
      }
    }
    arffWriter.flush();
    txtWriter.flush();
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
