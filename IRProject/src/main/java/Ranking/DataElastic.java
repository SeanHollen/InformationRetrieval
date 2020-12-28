package Ranking;

import org.apache.http.HttpHost;
import org.elasticsearch.client.*;
import org.elasticsearch.client.core.*;
import org.elasticsearch.client.indices.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DataElastic implements Data {

  private RestHighLevelClient client;

  // HashMap<term, dfScore> How many documents a term appears in
  private HashMap<String, Double> dfScores;
  // HashMap<docId, HashMap<term, tfScore>> How frequently a term appears in a document
  private HashMap<String, HashMap<String, Double>> tfScores;
  // HashMap<term, aggTfScore> For each given term, its total frequency across all documents
  private HashMap<String, Double> tfScores_agg;
  // HashMap<docId, docLength> The length of each document
  private HashMap<String, Double> docLengths;
  // Average length of docs
  private double avgDocLengths;
  // Total number of unique terms in the collection
  private double vocabSize = 0;
  // Total lengths of docs
  private double totalDocLengths;

  DataElastic() {
    client = new RestHighLevelClient(RestClient.builder(
            new HttpHost("localhost", 9200, "http")));
  }

  public void loadToMemory(ArrayList<String> docIds) {
    if (docIds.size() == 0) {
      throw new IllegalArgumentException("ne documents found by fetch function");
    }
    // HashMap<String term, Double dfScore>
    dfScores = new HashMap<>();
    // HashMap<String docId, HashMap<String term, Double tfScore>>
    tfScores = new HashMap<>();
    // HashMap<term, aggTfScore>
    tfScores_agg = new HashMap<>();
    // HashMap<String docId, Double docLength>
    docLengths = new HashMap<>();
    // avgDocLengths also updated
    vocabSize = 0;
    // Total length of documents
    totalDocLengths = 0;
    HashSet<String> allTerms = new HashSet<>(); // for use calculating vocab size
    int counter = 0;
    for (String docId : docIds) {
      counter++;
      if (counter % 1000 == 0) System.out.println("" + counter + " processed");
      int docLen = 0;
      tfScores.put(docId, new HashMap<>());
      TermVectorsRequest tvRequest = new TermVectorsRequest("api89", docId);
      tvRequest.setFields("content");
      tvRequest.setTermStatistics(true);
      TermVectorsResponse response;
      try {
        response = client.termvectors(tvRequest, RequestOptions.DEFAULT);
      } catch (IOException e) {
        e.printStackTrace();
        continue;
      }
      List<TermVectorsResponse.TermVector> TVR = response.getTermVectorsList();
      if (TVR == null || TVR.size() == 0) continue;
      for (TermVectorsResponse.TermVector tv : TVR) {
        for (TermVectorsResponse.TermVector.Term term : tv.getTerms()) {
          String st = term.getTerm();
          if (!allTerms.contains(st)) {
            allTerms.add(st);
          }
          dfScores.put(st, (double) term.getDocFreq());
          tfScores.get(docId).put(st, (double) term.getTermFreq());
          if (tfScores.containsKey(st)) {
            tfScores_agg.put(st, tfScores_agg.get(st) + term.getTermFreq());
          } else {
            tfScores_agg.put(st, (double) term.getTermFreq());
          }
          docLen += term.getTermFreq();
        }
      }
      docLengths.put(docId, (double) docLen);
      totalDocLengths += docLen;
    }
    avgDocLengths = totalDocLengths / docIds.size();
    vocabSize = allTerms.size();
    System.out.println("df Scores: " + dfScores);
    System.out.println("Average doc lengths: " + avgDocLengths);
    System.out.println("Vocab size: " + vocabSize);
    System.out.println("Document lengths: " + docLengths);
  }

  public void makeTermsQueryable(ArrayList<String> terms) {
  }

  // The term frequency of term in document
  public double tf(String docId, String term) {
    if (!tfScores.get(docId).containsKey(term)) {
      return 0;
    }
    return tfScores.get(docId).get(term);
  }

  public double tf_agg(String term) {
    if (!tfScores_agg.containsKey(term)) {
      tfScores_agg.put(term, (double) 0);
      return 0;
    }
    return tfScores_agg.get(term);
  }

  // The number of documents which contain term
  public double df(String term) {
    if (!dfScores.containsKey(term)) {
      System.out.println("Anomaly: none of the documents contain the term " + term);
      dfScores.put(term, (double) 0);
    }
    return dfScores.get(term);
  }

  // The length of a document
  public double docLen(String document) {
    if (!docLengths.containsKey(document)) {
      docLengths.put(document, (double) 0);
    }
    return docLengths.get(document);
  }

  public double vocabSize() {
    return vocabSize;
  }

  public double avgDocLengths() {
    return avgDocLengths;
  }

  public double totalDocLengths() {
    return totalDocLengths;
  }

  // Does stemming
  public HashMap<Integer, ArrayList<String>> getStemmed(HashMap<Integer, String> queries) {
    // HashMap<queryId, ArrayList<term>> The list of stemmed terms in each query
    HashMap<Integer, ArrayList<String>> stemmed = new HashMap<>();
    for (int queryId : queries.keySet()) {
      AnalyzeRequest analyzeRequest = AnalyzeRequest.buildCustomAnalyzer("standard")
              .addTokenFilter("lowercase")
              .addTokenFilter("stemmer")
              .build(queries.get(queryId));
      AnalyzeResponse response = null;
      try {
        response = client.indices().analyze(analyzeRequest, RequestOptions.DEFAULT);
      } catch (IOException e) {
        e.printStackTrace();
      }
      stemmed.put(queryId, new ArrayList<>());
      for (AnalyzeResponse.AnalyzeToken token : response.getTokens()) {
        stemmed.get(queryId).add(token.getTerm());
      }
    }
    return stemmed;
  }

}
