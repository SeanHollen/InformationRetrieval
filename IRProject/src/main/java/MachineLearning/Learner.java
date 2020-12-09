package MachineLearning;

import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import weka.classifiers.functions.LinearRegression;

import Indexing.PrivateIndexing;

public class Learner {

  private HashSet<String> allQueries;
  private HashSet<String> trainingQueries;
  private HashSet<String> testingQueries;

  ArrayList<String> queryIds;
  ArrayList<String> docIds;

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

  // todo
  public void performLinearRegression() throws Exception {
    String featureMatrix = "/path/to/feature-matrix.arff";
    Instances data = new Instances(new BufferedReader(new FileReader(featureMatrix)));
    data.setClassIndex(data.numAttributes() - 1);
    LinearRegression regressionModel = new LinearRegression();
    regressionModel.buildClassifier(data);
    Enumeration enumerateInstances = data.enumerateInstances();
    int n = 0;
    while (enumerateInstances.hasMoreElements()) {
      String queryId = queryIds.get(n);
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

    String resultPath = "/path/to/training-result-x.txt";
    File file = new File(resultPath);
    file.createNewFile();
    FileWriter myWriter = new FileWriter(resultPath);
    ArrayList<Integer> documentNums = new ArrayList<>(testRelevance.keySet());
    for (int queryNumber : documentNums) {
      PriorityQueue<DocScore> docScores = testRelevance.get(queryNumber);
      int rank = 0;
      while (docScores.size() != 0) {
        DocScore docScore = docScores.poll();
        rank++;
        String scoreStr = String.format("%.6f", docScore.getScore());
        myWriter.write("" + queryNumber + " Q0 " + docScore.getDocument() + " " + rank
                + " " + scoreStr + " Exp\n");
        if (rank >= 1000) {
          break;
        }
      }
    }
    myWriter.close();


//    String resultPath = "/path/to/training-result-x.txt";
//    BufferedWriter writer = new BufferedWriter(new FileWriter(resultPath));
//    ArrayList<String> testRelevanceQueryIds = new ArrayList<>(testRelevance.keySet());
//    Collections.sort(testRelevanceQueryIds);
//    for (String queryId : testRelevanceQueryIds) {
//
//    }
  }
}
