package Util;

public class DocScore implements Comparable<DocScore> {

  private String document;
  private Double score;

  public DocScore(String document, Double score) {
    this.document = document;
    this.score = score;
  }

  public int compareTo(DocScore other) {
    if (this.score > other.score) {
      return -1;
    } else if (this.score < other.score) {
      return 1;
    } else {
      return 0;
    }
  }

  public String getDocument() {
    return document;
  }

  public double getScore() {
    return score;
  }

}
