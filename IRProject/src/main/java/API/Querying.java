package API;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.TermVectorsRequest;
import org.elasticsearch.client.core.TermVectorsResponse;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class Querying {

  private RestHighLevelClient client;
  private String outFilePath = "/Users/sean.hollen/Desktop/IR_Class/test/IRProject/out";
  // HashMap<term, dfScore> How many documents a term appears in
  private HashMap<String, Double> dfScores;
  // HashMap<docId, HashMap<term, tfScore>> How frequently a term appears in a document
  private HashMap<String, HashMap<String, Double>> tfScores;
  // HashMap<term, aggTfScore> For each given term, its total frequency across all documents
  private HashMap<String, Double> tfScores_agg;
  // HashMap<docId, docLength> The length of each document
  private HashMap<String, Double> docLengths;
  // HashMap<queryId, ArrayList<term>> The list of stemmed terms in each query
  private HashMap<Integer, ArrayList<String>> stemmed;
  // Average length of docs
  private double avgDocLengths;
  // Total number of unique terms in the collection
  private double vocabSize = 0;
  // Total lengths of docs
  private double totalDocLengths;

  public Querying() {
    client = new RestHighLevelClient(RestClient.builder(
            new HttpHost("localhost", 9200, "http")));
  }

  // Uses helper methods, writes to file
  public void queryDocuments(HashMap<Integer, String> queries, ArrayList<String> docIds) {
    while (true) {
      System.out.println("Use es, okapi, tfidf, bm25, lm_laplace, lm_jm, & quit\n");
      // HashMap<query-number, HashMap<score, docno>>
      HashMap<Integer, HashMap<Double, String>> data = new HashMap<Integer, HashMap<Double, String>>();
      Scanner input = new Scanner(System.in);
      String command = input.next();
      if (command.equals("es")) {
        for (Integer qnum : queries.keySet()) {
          data.put(qnum, ESBuiltIn(queries.get(qnum)));
        }
      } else if (command.equals("quit")) {
        return;
      } else {
        if (vocabSize == 0) {
          this.fetch(queries, docIds);
        }
        try {
          data = this.calculate(command, docIds);
        } catch (Exception e) {
          System.out.println("there was an error! " + e);
        }
      }
      System.out.println(data);
      if (data.size() == 0) {
        continue;
      }
      System.out.println("finished calculation");
      String fileName = outFilePath + "/" + command + ".txt";
      // Writing to file
      try {
        File file = new File(fileName);
        if (file.createNewFile()) {
          System.out.println("File created: " + file.getName());
        } else {
          System.out.println("File already exists.");
        }
        FileWriter myWriter = new FileWriter(fileName);
        ArrayList<Integer> documentNums = new ArrayList<Integer>(data.keySet());
        Collections.sort(documentNums);
        for (Integer queryNumber : documentNums) {
          HashMap<Double, String> queryResult = data.get(queryNumber);
          ArrayList<Double> scores = new ArrayList<Double>(queryResult.keySet());
          Collections.sort(scores, Collections.reverseOrder());
          int rank = 0;
          for (Double score : scores) {
            rank++;
            String scoreStr = String.format("%.6f", score);
            myWriter.write("" + queryNumber + " Q0 " + queryResult.get(score) + " " + rank
                    + " " + scoreStr + " Exp\n");
            if (rank >= 1000) {
              break;
            }
          }
        }
        myWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println("wrote to file " + fileName);
    }
  }

  private void fetch(HashMap<Integer, String> queries, ArrayList<String> docIds) {
    if (queries.size() == 0) {
      throw new IllegalArgumentException("no queries found by fetch function");
    }
    if (docIds.size() == 0) {
      throw new IllegalArgumentException("ne documents found by fetch function");
    }
    // Does stemming
    // HashMap<Integer queryId, ArrayList<String term>> stemmedWords (DONE)
    stemmed = new HashMap<Integer, ArrayList<String>>();
    HashSet<String> stemmedWordsHashSet = new HashSet<String>();
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
      stemmed.put(queryId, new ArrayList<String>());
      for (AnalyzeResponse.AnalyzeToken token : response.getTokens()) {
        stemmed.get(queryId).add(token.getTerm());
        stemmedWordsHashSet.add(token.getTerm());
      }
    }
    System.out.println(stemmedWordsHashSet.toString());
    // HashMap<String term, Double dfScore>
    dfScores = new HashMap<String, Double>();
    // HashMap<String docId, HashMap<String term, Double tfScore>>
    tfScores = new HashMap<String, HashMap<String, Double>>();
    // HashMap<term, aggTfScore>
    tfScores_agg = new HashMap<String, Double>();
    // HashMap<String docId, Double docLength>
    docLengths = new HashMap<String, Double>();
    // avgDocLengths also updated
    vocabSize = 0;
    // Total length of documents
    totalDocLengths = 0;
    // todo: use builtin to do this faster?
    HashSet<String> allTerms = new HashSet<String>(); // for use calculating vocab size
    int counter = 0;
    for (String docId : docIds) {
      counter++;
      if (counter % 1000 == 0) System.out.println("" + counter + " processed");
      int docLen = 0;
      tfScores.put(docId, new HashMap<String, Double>());
      TermVectorsRequest tvRequest = new TermVectorsRequest("api89", docId);
      tvRequest.setFields("content");
      tvRequest.setTermStatistics(true);
      TermVectorsResponse response = null;
      try {
        response = client.termvectors(tvRequest, RequestOptions.DEFAULT);
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (response == null) continue;
      List<TermVectorsResponse.TermVector> TVR = response.getTermVectorsList();
      if (TVR == null || TVR.size() == 0) continue;
      for (TermVectorsResponse.TermVector tv : TVR) {
        for (TermVectorsResponse.TermVector.Term term : tv.getTerms()) {
          String st = term.getTerm();
          if (!allTerms.contains(st)) {
            allTerms.add(st);
          }
          if (stemmedWordsHashSet.contains(st)) {
            dfScores.put(st, (double) term.getDocFreq());
            tfScores.get(docId).put(st, (double) term.getTermFreq());
            if (tfScores.containsKey(st)) {
              tfScores_agg.put(st, tfScores_agg.get(st) + term.getTermFreq());
            } else {
              tfScores_agg.put(st, (double) term.getTermFreq());
            }
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

  private HashMap<Integer, HashMap<Double, String>> calculate(
          String command, ArrayList<String> docIds) {
    double totalDocs = docIds.size();
    HashMap<Integer, HashMap<Double, String>> data = new HashMap<Integer, HashMap<Double, String>>();
    double score = 0;
    if (command.equals("bm25")) {
      for (Integer qnum : stemmed.keySet()) {
        HashMap<String, Integer> stemmedAsCounter = new HashMap<String, Integer>();
        for (String st : stemmed.get(qnum)) {
          if (stemmedAsCounter.containsKey(st)) {
            stemmedAsCounter.put(st, stemmedAsCounter.get(st) + 1);
          } else {
            stemmedAsCounter.put(st, 1);
          }
        }
        System.out.println(stemmedAsCounter);
        data.put(qnum, new HashMap<Double, String>());
        for (String docID : docIds) {
          score = Okapi_BM25(docID, stemmedAsCounter, totalDocs);
          data.get(qnum).put(score, docID);
        }
        System.out.println("+1 query done");
      }
    } else if (!(command.equals("okapi") || command.equals("tfidf") ||
              command.equals("lm_laplace") || command.equals("lm_jm"))) {
        System.out.println("not a command");
    } else {
      for (Integer qnum : stemmed.keySet()) {
        data.put(qnum, new HashMap<Double, String>());
        for (String docId : docIds) {
          if (command.equals("okapi")) {
            score = Okapi_TF(docId, stemmed.get(qnum));
          } else if (command.equals("tfidf")) {
            score = TF_IDF(docId, stemmed.get(qnum), totalDocs);
          } else if (command.equals("lm_laplace")) {
            score = Unigram_LM_Laplace(docId, stemmed.get(qnum));
          } else if (command.equals("lm_jm")) {
            score = Unigram_LM_Jelinek_Mercer(docId, stemmed.get(qnum));
          }
          data.get(qnum).put(score, docId);
        }
        System.out.println("+1 query done");
      }
    }
    return data;
  }

  // The term frequency of term in document document
  private double tf(String docId, String term) {
    if (!tfScores.get(docId).containsKey(term)) {
      return 0;
    }
    return tfScores.get(docId).get(term);
  }

  private double tf_agg(String term) {
    if (!tfScores_agg.containsKey(term)) {
      tfScores_agg.put(term, (double) 0);
      return 0;
    }
    return tfScores_agg.get(term);
  }

  // The number of documents which contain term
  private double df(String term) {
    if (!dfScores.containsKey(term)) {
      System.out.println("Anomaly: none of the documents contain the term " + term);
      dfScores.put(term, (double) 0);
    }
    return dfScores.get(term);
  }

  // The length of a document
  private double docLen(String document) {
    if (!docLengths.containsKey(document)) {
      docLengths.put(document, (double) 0);
    }
    return docLengths.get(document);
  }

  private HashMap<Double, String> ESBuiltIn(String query) {
    // SearchRequest -> SearchSourceBuilder -> MatchQueryBuilder -> SearchResponse
    SearchRequest searchRequest = new SearchRequest("api89");
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchQuery("content", query));
    searchSourceBuilder.size(1000);
    searchRequest.source(searchSourceBuilder);
    SearchResponse searchResponse;
    try {
      searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    HashMap<Double, String> results = new HashMap<Double, String>();
    for (SearchHit hit : searchResponse.getHits()) {
      results.put((double) hit.getScore(), hit.getId());
    }
    System.out.println(results);
    return results;
  }

  private double Okapi_TF(String document, ArrayList<String> query) {
    double sum = 0;
    double docLen = docLen(document);
    for (String word : query) {
      double tf = tf(document, word);
      sum += (tf / (tf + 0.5 + 1.5 * docLen / avgDocLengths));
    }
    return sum;
  }

  private double TF_IDF(String document, ArrayList<String> query, double numDocs) {
    double sum = 0;
    double docLen = docLen(document);
    for (String word : query) {
      double tf = tf(document, word);
      double okapi_TF = tf / (tf + 0.5 + 1.5 * docLen / avgDocLengths);
      sum += okapi_TF * Math.log(numDocs / df(word));
    }
    return sum;
  }

  private double Okapi_BM25(String document, HashMap<String, Integer> query, double numDocs) {
    double sum = 0;
    double docLen = docLen(document);
    double k1 = 1.2;
    double k2 = 500; // 0 < k2 < 1000
    double b = 0.75;
    for (String word : query.keySet()) {
      double tf = tf(document, word);
      double first = Math.log((numDocs + 0.5) / (df(word) + 0.5));
      double second_prefix = docLen / avgDocLengths;
      double second = (tf + k1 * tf) / (tf + k1 * ((1 - b) + b * second_prefix));
      double tfInquiry = (double) query.get(word);
      double third = (tfInquiry + k2 * tfInquiry) / (tfInquiry + k2);
      sum += (first * second * third);
    }
    return sum;
  }

  private double Unigram_LM_Laplace(String document, ArrayList<String> query) {
    double sum = 0;
    double docLen = docLen(document);
    for (String word : query) {
      sum += Math.log((tf(document, word) + 1) / (docLen + vocabSize));
    }
    return sum;
  }

  private double Unigram_LM_Jelinek_Mercer(String document, ArrayList<String> query) {
    double sum = 0;
    double lambda = .99; // SET: 0.5 < lambda < 1 based on empiricism
    double docLen = docLen(document);
    for (String word : query) {
      double tf = tf(document, word);
      double a = lambda * tf / docLen;
      double b = (1 - lambda) * (tf_agg(word) / totalDocLengths);
      sum += Math.log(a + b);
    }
    return sum;
  }

}
