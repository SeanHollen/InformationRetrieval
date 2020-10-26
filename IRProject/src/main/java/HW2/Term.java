package HW2;

import java.util.ArrayList;
import org.javatuples.Triplet;

public class Term {

  int docFreq;
  int ttf;
  // ArrayList<Triplet<docId, termFrequency, ArrayList<termPosition>>>
  ArrayList<Triplet<String, Integer, ArrayList<String>>> docsIn;

}
