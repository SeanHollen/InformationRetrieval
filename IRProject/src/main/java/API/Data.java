package API;

import java.util.ArrayList;
import java.util.HashMap;

public interface Data {

  // The term frequency of term in document
  double tf(String docId, String term);
  double tf_agg(String term);
  // The number of documents which contain term
  double df(String term);
  // The length of a document
  double docLen(String document);
  void fetch(HashMap<Integer, String> queries, ArrayList<String> docIds);
  double vocabSize();
  double avgDocLengths();
  double totalDocLengths();
  HashMap<Integer, ArrayList<String>> getStemmed();
  void prepareForQuery(ArrayList<String> terms);

}
