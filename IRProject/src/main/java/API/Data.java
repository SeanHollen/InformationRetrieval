package API;

import java.util.ArrayList;
import java.util.HashMap;

public interface Data {

  double tf(String docId, String term);
  double tf_agg(String term);
  double df(String term);
  double docLen(String document);
  void fetch(HashMap<Integer, String> queries, ArrayList<String> docIds);
  double vocabSize();
  double avgDocLengths();
  double totalDocLengths();
  HashMap<Integer, ArrayList<String>> getStemmed();

}
