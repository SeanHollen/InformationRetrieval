package Indexing;

public class TermPosition implements Comparable<TermPosition> {

  private String termString;
  private int termHash;
  private int docHash;
  private int position;

  TermPosition(int termHash, int docHash, int position) {
    this.termHash = termHash;
    this.docHash = docHash;
    this.position = position;
    this.termString = "";
  }

  TermPosition(int termHash, String termString, int docHash, int position) {
    this.termHash = termHash;
    this.termString = termString;
    this.docHash = docHash;
    this.position = position;
  }

  public int getTermHash() {
    return this.termHash;
  }

  public int getDocHash() {
    return this.docHash;
  }

  public int getPosition() {
    return this.position;
  }

  public int compareTo(TermPosition other) {
    if (this.termString == null || other.termString == null) {
      System.out.println(this.termHash + " vs " + other.termHash);
      // todo I have no goddamn Idea why this doesn't work. Hence using strings instead (below)
      return this.termHash - other.termHash;
    }
    return this.termString.compareTo(other.termString);
  }

  // For printing an array of TP
  public String toString() {
    return termString + "(" + termHash + ":" + docHash + ":" + position + ")";
  }

}
