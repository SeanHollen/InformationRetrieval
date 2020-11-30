package Evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;

public class Evaluator {

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

  public static void evaluate(String qrelFile, String resultsFile) throws FileNotFoundException {
    BufferedReader qrelFileReader = new BufferedReader(new FileReader(new File(qrelFile)));
    BufferedReader resultsFileReader = new BufferedReader(new FileReader(new File(qrelFile)));

  }
}
