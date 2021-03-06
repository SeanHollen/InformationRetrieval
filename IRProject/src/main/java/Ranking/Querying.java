package Ranking;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import java.io.IOException;
import java.util.*;
import Util.DocScore;
import Util.Paths;
import Util.ResultsPrinter;

public class Querying {

  private final int TRUNCATE_RESULTS_AT = 1000000;
  private Data data;
  private boolean fetched;
  // HashMap<query-number, HashMap<score, docno>>
  private HashMap<Integer, PriorityQueue<DocScore>> results;

  public Querying() {
    this.data = new DataElastic();
    this.fetched = false;
    results = new HashMap<>();
  }

  public Querying(Data data) {
    this.data = data;
    this.fetched = false;
    results = new HashMap<>();
  }

  // writes to file
  public void queryDocuments(HashMap<Integer, String> queries, ArrayList<String> docIds) {
    while (true) {
      System.out.println("Use es, okapi, tfidf, bm25, lm_laplace, lm_jm, & quit\n");
      Scanner input = new Scanner(System.in);
      String command = input.next();
      if (command.equals("es")) {
        for (Integer qnum : queries.keySet()) {
          results.put(qnum, ESBuiltIn(queries.get(qnum)));
        }
      } else if (command.equals("quit")) {
        return;
      } else {
        if (!fetched) {
          data.loadToMemory(docIds);
          this.fetched = true;
        }
        try {
          this.calculate(command, docIds, queries);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      System.out.println("finished calculation");
      if (results.size() == 0) {
        System.out.println("failure to compute results");
        continue;
      }
      String fileName = Paths.rankingResults + "/" + command + ".txt";
      try {
        ResultsPrinter.resultsToFile(fileName, results, TRUNCATE_RESULTS_AT);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void calculate(String command, ArrayList<String> docIds,
                         HashMap<Integer, String> queries) {
    double totalDocs = docIds.size();
    double score = 0;
    HashMap<Integer, ArrayList<String>> stemmed = data.getStemmed(queries);
    if (command.equals("bm25")) {
      for (Integer qnum : stemmed.keySet()) {
        data.makeTermsQueryable(stemmed.get(qnum));
        HashMap<String, Integer> stemmedAsCounter = new HashMap<>();
        for (String st : stemmed.get(qnum)) {
          if (stemmedAsCounter.containsKey(st)) {
            stemmedAsCounter.put(st, stemmedAsCounter.get(st) + 1);
          } else {
            stemmedAsCounter.put(st, 1);
          }
        }
        System.out.println(stemmedAsCounter);
        results.put(qnum, new PriorityQueue<>());
        for (String docID : docIds) {
          score = Okapi_BM25(docID, stemmedAsCounter, totalDocs);
          results.get(qnum).add(new DocScore(docID, score));
        }
        System.out.println("+1 query done, results: " + results.get(qnum).size());
      }
    } else if (command.equals("okapi") || command.equals("tfidf")
            || command.equals("lm_laplace") || command.equals("lm_jm")) {
      for (Integer qnum : stemmed.keySet()) {
        System.out.println("starting new query");
        data.makeTermsQueryable(stemmed.get(qnum));
        System.out.println("successfully pulled data for query");
        results.put(qnum, new PriorityQueue<>());
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
          results.get(qnum).add(new DocScore(docId, score));
        }
        System.out.println("+1 query done, results: " + results.size() + ":" + results.get(qnum).size());
      }
    } else {
      System.out.println("not a command");
    }
  }

  private PriorityQueue<DocScore> ESBuiltIn(String query) {
    RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
            new HttpHost("localhost", 9200, "http")));
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
    PriorityQueue<DocScore> results = new PriorityQueue<>();
    for (SearchHit hit : searchResponse.getHits()) {
      results.add(new DocScore(hit.getId(), (double) hit.getScore()));
    }
    System.out.println(results);
    return results;
  }

  private double Okapi_TF(String document, ArrayList<String> query) {
    double sum = 0;
    double docLen = data.docLen(document);
    for (String word : query) {
      double tf = data.tf(document, word);
      sum += (tf / (tf + 0.5 + 1.5 * docLen / data.avgDocLengths()));
    }
    return sum;
  }

  private double TF_IDF(String document, ArrayList<String> query, double numDocs) {
    double sum = 0;
    double docLen = data.docLen(document);
    for (String word : query) {
      double tf = data.tf(document, word);
      double okapi_TF = tf / (tf + 0.5 + 1.5 * docLen / data.avgDocLengths());
      sum += okapi_TF * Math.log(numDocs / data.df(word));
    }
    return sum;
  }

  private double Okapi_BM25(String document, HashMap<String, Integer> query, double numDocs) {
    double sum = 0;
    double docLen = data.docLen(document);
    double k1 = 1.2;
    double k2 = 500; // 0 < k2 < 1000
    double b = 0.75;
    for (String word : query.keySet()) {
      double tf = data.tf(document, word);
      double first = Math.log((numDocs + 0.5) / (data.df(word) + 0.5));
      double second_prefix = docLen / data.avgDocLengths();
      double second = (tf + k1 * tf) / (tf + k1 * ((1 - b) + b * second_prefix));
      double tfInquiry = (double) query.get(word);
      double third = (tfInquiry + k2 * tfInquiry) / (tfInquiry + k2);
      sum += (first * second * third);
    }
    return sum;
  }

  private double Unigram_LM_Laplace(String document, ArrayList<String> query) {
    double sum = 0;
    double docLen = data.docLen(document);
    for (String word : query) {
      sum += Math.log((data.tf(document, word) + 1) / (docLen + data.vocabSize()));
    }
    return sum;
  }

  private double Unigram_LM_Jelinek_Mercer(String document, ArrayList<String> query) {
    double sum = 0;
    double lambda = .99; // SET: 0.5 < lambda < 1 based on empiricism
    double docLen = data.docLen(document);
    for (String word : query) {
      double tf = data.tf(document, word);
      double a = lambda * tf / docLen;
      double b = (1 - lambda) * (data.tf_agg(word) / data.totalDocLengths());
      sum += Math.log(a + b);
    }
    return sum;
  }

}
