package MachineLearning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class DocManager {

  private HashSet<String> allQueries;
  private HashSet<String> testingQueries;
  private HashSet<String> trainingQueries;

  public HashSet<String> getAllQueries() {
    return allQueries;
  }

  public void segregateQueries() {
    // todo
  }

  public HashSet<String> getTrainingQueries() {
    return trainingQueries;
  }

  public HashSet<String> getTestingQueries() {
    return testingQueries;
  }

  public HashMap<Integer, PriorityQueue<DocScore>> getQRELMap() {
    // todo
    return new HashMap<>();
  }
}
