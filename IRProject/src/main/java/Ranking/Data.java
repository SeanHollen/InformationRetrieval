package Ranking;

import java.util.ArrayList;
import java.util.HashMap;

public interface Data {

  // **** USE ****
  // FETCH: call first
  void fetch(HashMap<Integer, String> queries, ArrayList<String> docIds);
  // PREPARE FOR QUERY: call before calling tf/df/etc on any of the terms in the query
  void prepareForQuery(ArrayList<String> terms);

  // The term frequency of term in document
  double tf(String docId, String term);
  double tf_agg(String term);
  // The number of documents which contain term
  double df(String term);
  // The length of a document
  double docLen(String document);
  double vocabSize();
  double avgDocLengths();
  double totalDocLengths();
  HashMap<Integer, ArrayList<String>> getStemmed();

}
