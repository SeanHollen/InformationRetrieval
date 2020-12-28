package MachineLearning;

import org.javatuples.Pair;
import weka.core.Instance;
import weka.core.Instances;
import java.io.*;
import java.util.*;
import weka.classifiers.functions.LinearRegression;
import Util.DocScore;
import Util.ResultsPrinter;

public class Learner {

  private final String arffPath = "out/MachineLearning/feature-matrix.arff";

  private HashSet<Integer> trainingQueries;
  private HashSet<Integer> testingQueries;

  // HashMap<QueryNumber, HashMap<Score, DocIds>>
  private HashMap<Integer, PriorityQueue<DocScore>> trainingRelevance;
  private HashMap<Integer, PriorityQueue<DocScore>> testRelevance;

  // ArrayList<Tuple<QueryIds, DocumentIds>
  private ArrayList<Pair<Integer, String>> arffFileLineInfo;

  public Learner() {
    testRelevance = new HashMap<>();
    trainingRelevance = new HashMap<>();
  }

  public void startManager(ArrayList<Integer> queryIds) throws IOException {
    String qrelFilePath = "IR_Data/AP_DATA/qrels.adhoc.51-100.AP89.txt";
    String rankingResultsPath = "out/RankingResults";
    String txtPath = "out/MachineLearning/feature-matrix.txt";
    DocManager manager = new DocManager(queryIds, 5);
    manager.generateQrelMap(qrelFilePath);
    manager.generateMatrix(arffPath, txtPath, rankingResultsPath);
    trainingQueries = manager.getTrainingQueries();
    testingQueries = manager.getTestingQueries();
    for (int queryId : queryIds) {
      trainingRelevance.put(queryId, new PriorityQueue<>());
      testRelevance.put(queryId, new PriorityQueue<>());
    }
  }

  public void performLinearRegression() throws Exception {
    Instances data = new Instances(new BufferedReader(new FileReader(arffPath)));
    data.setClassIndex(data.numAttributes() - 1);
    LinearRegression regressionModel = new LinearRegression();
    regressionModel.buildClassifier(data);
    System.out.println("model: " + regressionModel);
    Enumeration enumeratedInstances = data.enumerateInstances();
    int n = 0;
    while (enumeratedInstances.hasMoreElements()) {
      int queryId = arffFileLineInfo.get(n).getValue0();
      String docId = arffFileLineInfo.get(n).getValue1();
      Instance dataElement = (Instance) enumeratedInstances.nextElement();
      double result = regressionModel.classifyInstance(dataElement);
      if (testingQueries.contains(queryId)) {
        testRelevance.get(queryId).add(new DocScore(docId, result));
      } else if (trainingQueries.contains(queryId)) {
        trainingRelevance.get(queryId).add(new DocScore(docId, result));
      }
      n++;
    }
  }

  public void writeResultsToFile() {
    String trainingResultsFile = "out/MachineLearning/training-results.txt";
    String testingResultsFile = "out/MachineLearning/testing-results.txt";
    try {
      ResultsPrinter.resultsToFile(trainingResultsFile, trainingRelevance, 1000);
      ResultsPrinter.resultsToFile(testingResultsFile, testRelevance, 1000);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
