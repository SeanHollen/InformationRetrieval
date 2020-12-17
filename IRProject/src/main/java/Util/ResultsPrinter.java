package Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

public class ResultsPrinter {

  // results = HashMap<QueryNumber, HashMap<Score, DocIds>>
  public static void resultsToFile(String path, HashMap<Integer, PriorityQueue<DocScore>> results,
                                   int truncateResultsAt) throws IOException {
    File file = new File(path);
    file.createNewFile();
    FileWriter fileWriter = new FileWriter(path);
    ArrayList<Integer> queryNums = new ArrayList<>(results.keySet());
    Collections.sort(queryNums);
    for (int queryNumber : queryNums) {
      PriorityQueue<DocScore> queryResults = results.get(queryNumber);
      int rank = 0;
      while (queryResults.size() != 0) {
        DocScore docScore = queryResults.poll();
        rank++;
        String scoreStr = String.format("%.6f", docScore.getScore());
        fileWriter.write("" + queryNumber + " Q0 " + docScore.getDocument() + " " + rank + " "
                + scoreStr + " Exp\n");
        if (rank >= truncateResultsAt) {
          break;
        }
      }
      fileWriter.flush();
    }
    fileWriter.close();
    System.out.println("wrote to file " + path);
  }

}
