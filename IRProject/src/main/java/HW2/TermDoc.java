package HW2;

import java.util.ArrayList;

public class TermDoc {

  private int docHash;
  private int occurrences;
  private ArrayList<Integer> positions;

  public TermDoc(int docHash) {
    this.docHash = docHash;
    this.occurrences = 0;
    this.positions = new ArrayList<Integer>();
  }

  public TermDoc givePositions(ArrayList<Integer> newPosns) {
    occurrences += newPosns.size();
    positions.addAll(newPosns);
    return this;
  }

  public void addPosn(Integer newPosn) {
    occurrences++;
    positions.add(newPosn);
  }

  public String toString() {
    StringBuilder b = new StringBuilder(docHash + "|" + occurrences + "|");
    for (Integer i : positions) {
      b.append(i);
    }
    return b.toString();
  }
}
