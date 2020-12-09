package MachineLearning;

import weka.core.Instance;
import weka.core.Instances;
import java.io.*;
import java.io.IOException;
import java.util.*;
import weka.classifiers.functions.LinearRegression;

public class Learner {

  private HashSet<Integer> allQueries;
  private HashSet<Integer> trainingQueries;
  private HashSet<Integer> testingQueries;

  private ArrayList<Integer> queryIds;
  private ArrayList<String> docIds;

  // HashMap<QueryNumber, HashMap<Score, DocIds>>
  public HashMap<Integer, PriorityQueue<DocScore>> queryIDocIdRelevanceMap;
  public HashMap<Integer, PriorityQueue<DocScore>> testRelevance;
  public HashMap<Integer, PriorityQueue<DocScore>> trainingRelevance;

  public static void main(String[] args) throws IOException {
    Learner learner = new Learner();
    learner.readDataMatrix();
    DocManager manager = new DocManager();
    learner.allQueries.addAll(manager.getAllQueries());
    System.out.println(learner.allQueries.size());
    learner.queryIDocIdRelevanceMap = manager.getQRELMap();
    manager.segregateQueries();
    learner.trainingQueries = manager.getTrainingQueries();
    learner.testingQueries = manager.getTestingQueries();
    // todo
  }

  private void readDataMatrix() {

  }

  public void performLinearRegression() throws Exception {
    String featureMatrix = "/path/to/feature-matrix.arff";
    Instances data = new Instances(new BufferedReader(new FileReader(featureMatrix)));
    data.setClassIndex(data.numAttributes() - 1);
    LinearRegression regressionModel = new LinearRegression();
    regressionModel.buildClassifier(data);
    Enumeration enumerateInstances = data.enumerateInstances();
    int n = 0;
    while (enumerateInstances.hasMoreElements()) {
      int queryId = queryIds.get(n);
      String docId = docIds.get(n);
      Instance dataElement = (Instance) enumerateInstances.nextElement();
      double result = regressionModel.classifyInstance(dataElement);
      if (testingQueries.contains(queryId)) {
        testRelevance.get(queryId).add(new DocScore(docId, result));
      } else if (trainingQueries.contains(queryId)) {
        trainingRelevance.get(queryId).add(new DocScore(docId, result));
      }
      n++;
    }
    String testingResultsPath = "/path/to/testing-result-x.txt";
    resultsToFile(testingResultsPath, testRelevance);
    String trainingResultsPath = "/path/to/training-result-x.txt";
    resultsToFile(trainingResultsPath, trainingRelevance);
  }

  // todo: make util directory and move this to that
  public void resultsToFile(String path, HashMap<Integer,
          PriorityQueue<DocScore>> results) throws IOException {
    File file = new File(path);
    file.createNewFile();
    FileWriter writer = new FileWriter(path);
    ArrayList<Integer> queryNums = new ArrayList<>(results.keySet());
    for (int queryNumber : queryNums) {
      PriorityQueue<DocScore> docScores = results.get(queryNumber);
      int rank = 0;
      while (docScores.size() != 0) {
        DocScore docScore = docScores.poll();
        rank++;
        String scoreStr = String.format("%.6f", docScore.getScore());
        writer.write("" + queryNumber + " Q0 " + docScore.getDocument() + " " + rank
                + " " + scoreStr + " Exp\n");
        if (rank >= 1000) {
          break;
        }
      }
    }
    writer.close();
  }

}
