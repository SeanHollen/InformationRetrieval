package HW2;

public class TermPosition implements Comparable<TermPosition> {

  private int termHash;
  private int docHash;
  private int position;

  public TermPosition(int termHash, int docHash, int position) {
    this.termHash = termHash;
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
    return this.termHash - other.termHash;
  }
}
