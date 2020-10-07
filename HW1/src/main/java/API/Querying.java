package API;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Querying {

  private RestHighLevelClient client;

  public Querying() {
    client = new RestHighLevelClient(RestClient.builder(
            new HttpHost("localhost", 9200, "http")));
  }

  // TODO
  public void queryDocuments(HashMap<Integer, String> queries, ArrayList<String> docIds) {
    System.out.println("Use es, okapi, tfidf, bm25, lm_laplace, & lm_jm");
    // HashMap<query-number, HashMap<score, docno>>
    HashMap<Integer, HashMap<Double, String>> data = new HashMap<Integer, HashMap<Double, String>>();
    Scanner input = new Scanner(System.in);
    String command = input.next();
    if (command.equals("es")) {
      for (Integer qnum : queries.keySet()) {
        data.put(qnum, ESBuiltIn(queries.get(qnum)));
      }
    } else if (command.equals("okapi")) {
      for (String docID : docIds) {

      }
    } else if (command.equals("tfidf")) {

    } else if (command.equals("bm25")) {

    } else if (command.equals("lm_laplace")) {

    } else if (command.equals("lm_jm")) {

    } else {
      System.out.println("not a command");
    }
    System.out.println(data);
    System.out.println("done");
  }

  // TODO
  private double tf(String document, String word) {
    return 0;
  }

  // TODO
  private double df(String word) {
    return 0;
  }

  // TODO
  private double docLen(String document) {
    return 0;
  }

  private HashMap<Double, String> ESBuiltIn(String query) {
    // SearchRequest -> SearchSourceBuilder -> MatchQueryBuilder -> SearchResponse
    SearchRequest searchRequest = new SearchRequest("api89");
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchQuery("content", query));
    searchRequest.source(searchSourceBuilder);
    SearchResponse searchResponse;
    try {
      searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    HashMap<Double, String> results = new HashMap<Double, String>();
    for (SearchHit hit : searchResponse.getHits()) {
      results.put((double)hit.getScore(), hit.getId());
    }
    System.out.println(results);
    return results;
  }

  private double Okapi_TF(String document, String[] query, double avgDocLen) {
    double sum = 0;
    double docLen = docLen(document);
    for (String word : query) {
      double tf = tf(document, word);
      sum += tf / (tf + 0.5 + 1.5 * docLen / avgDocLen);
    }
    return sum;
  }

  private double TF_IDF(String document, String[] query, double avgDocLen, double totalDocs) {
    double sum = 0;
    double docLen = docLen(document);
    for (String word : query) {
      double tf = tf(document, word);
      double okapi_TF = tf / (tf + 0.5 + 1.5 * docLen / avgDocLen);
      sum += okapi_TF + Math.log(totalDocs / df(word));
    }
    return sum;
  }

  private double Okapi_BM25(String document, String[] query, double avgDocLen, double numDocs) {
    double sum = 0;
    double docLen = docLen(document);
    double k1 = 0;
    double k2 = 0;
    double b = 0;
    for (String word : query) {
      double tf = tf(document, word);
      double first = Math.log((numDocs + 0.5)/(0.5 + df(word)));
      double second_prefix = docLen / avgDocLen;
      double second = (tf + k1 * tf) / (tf + k1 * ((1 - b) + b * second_prefix));
      double third = (tf + k2 * tf) / (tf + k2);
      sum += first * second * third;
    }
    return sum;
  }

  private double Unigram_LM_Laplace(String document, String[] query, double vocabSize) {
    double sum = 0;
    double docLen = docLen(document);
    for (String word : query) {
      double td = 0;
      sum += (td + 1) / (docLen + vocabSize);
    }
    return sum;
  }

  private double Unigram_LM_Jelinek_Mercer(String document, String[] query) {
    double sum = 0;
    double lambda = 0;
    double docLen = docLen(document);
    for (String word : query) {
      double tf = tf(document, word);
      double a = lambda * tf / docLen;
      double b = (1 - lambda) * 100; // TODO
      sum += Math.log(a + b);
    }
    return sum;
  }

}
