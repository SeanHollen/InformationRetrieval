package MachineLearning;

import weka.core.Instance;
import weka.core.Instances;
import java.io.*;
import java.util.*;
import weka.classifiers.functions.LinearRegression;

import Util.DocScore;

public class Learner {

  private final String arffPath = "out/MachineLearning/feature-matrix.arff";
  private final String txtPath = "out/MachineLearning/feature-matrix.txt";

  private HashSet<Integer> trainingQueries;
  private HashSet<Integer> testingQueries;

  private ArrayList<Integer> queryIds;
  private ArrayList<String> docIds;

  // HashMap<QueryNumber, HashMap<Score, DocIds>>
  private HashMap<Integer, PriorityQueue<DocScore>> testRelevance;
  private HashMap<Integer, PriorityQueue<DocScore>> trainingRelevance;
  private HashMap<Integer, PriorityQueue<DocScore>> results;

  public void start(ArrayList<Integer> queryIds, ArrayList<String> docIds)
          throws IOException {
    String qrelFilePath = "IR_Data/AP_DATA/qrels.adhoc.51-100.AP89.txt";
    String rankingResultsPath = "out/RankingResults";
    this.queryIds = queryIds;
    this.docIds = docIds;
    DocManager manager = new DocManager(queryIds, 5);
    manager.generateQrelMap(qrelFilePath);
    manager.generateMatrix(arffPath, txtPath, rankingResultsPath);
    trainingQueries = manager.getTrainingQueries();
    testingQueries = manager.getTestingQueries();
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
  }

}
