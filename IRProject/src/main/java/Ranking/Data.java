package Ranking;

import java.util.ArrayList;
import java.util.HashMap;

public interface Data {

  // **** USE ****
  // LOAD TO MEMORY: call first
  void loadToMemory(ArrayList<String> docIds);
  // PREPARE FOR QUERY: call before calling tf/df/etc on any of the terms in the query
  void makeTermsQueryable(ArrayList<String> terms);
  // GET STEMMED: get these stemmed words to do the query with
  HashMap<Integer, ArrayList<String>> getStemmed(HashMap<Integer, String> queries);

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

}
