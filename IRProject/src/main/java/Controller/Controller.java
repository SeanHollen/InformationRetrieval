package Controller;

import java.io.*;
import java.util.*;
import Evaluation.Evaluator;
import Ranking.DataPrivate;
import Indexing.ElasticIndexing;
import Ranking.Querying;
import Crawler.Crawler;
import Indexing.PrivateIndexing;
import Parsers.*;


public class Controller {

  private ElasticIndexing indexing;
  private HashMap<Integer, String> queries = new HashMap<>();
  private HashMap<String, String> documents = new HashMap<>();
  private HashSet<String> stopwords;
  private HashMap<String, String> stemwords;
  private PrivateIndexing tokenizer;
  private final String queryFile = "IR_Data/AP_DATA/queries_v4.txt";
  private final String docsToParse = "IR_Data/AP_DATA/ap89_collection";
  private final String sitesToParse = "out/CrawledDocuments";
  private final String stopWordsFile = "IR_Data/AP_DATA/stoplist.txt";
  private final String stemmerFile = "IR_Data/AP_DATA/stem-classes.lst";
  private final String mergeDataPath = "IndexData";
  private final String privateMergeDataPath = "out/CrawledDocuments";
  private final String qrelFile = "IR_Data/AP_DATA/qrels.adhoc.51-100.AP89.txt";
  private final String resultsFiles = "out/RankingResults";

  private void checkTokenizer() {
    if (tokenizer == null) {
      if (stopwords == null) {
        throw new IllegalArgumentException("stopwords not found, try standardStart");
      } else if (stemwords == null) {
        throw new IllegalArgumentException("stemwords not found, try standardStart");
      }
      tokenizer = new PrivateIndexing(stopwords, stemwords);
    }
  }

  public void parseQueries() {
    QueryParser queryParser = new QueryParser();
    try {
      queries = queryParser.parseFile(queryFile);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    System.out.println(queries);
  }

  public void parseWebsites() {
    parseContent(sitesToParse);
  }

  public void parseTestDocuments() {
    parseContent(docsToParse);
  }

  private void parseContent(String dir) {
    TRECparser parser = new TRECparser();
    try {
      documents = parser.parseFiles(dir);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    System.out.println(documents.keySet().size());
  }

  public void createElasticIndex() {
    indexing = new ElasticIndexing("personal");
    indexing.setIndex("api89");
    try {
      indexing.createIndex();
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    System.out.println("created Index");
  }

  public void postFiles() {
    if (indexing == null) {
      indexing = new ElasticIndexing("personal");
    }
    indexing.postDocuments(documents);
    System.out.println("posted documents");
  }

  public void teamCloud() {
    if (indexing == null) {
      indexing = new ElasticIndexing("team");
    }
    indexing.setIndex("hw3");
  }

  public void queryElastic() {
    Querying querying = new Querying();
    querying.queryDocuments(queries, new ArrayList<>(documents.keySet()));
  }

  public void queryPrivate() {
    checkTokenizer();
    Querying querying = new Querying(new DataPrivate(tokenizer));
    querying.queryDocuments(queries, new ArrayList<>(documents.keySet()));
  }

  public void parseStemming() {
    try {
      ParseStopwords sw = new ParseStopwords();
      stopwords = sw.parseFile(stopWordsFile);
      StemmerParser stemmer = new StemmerParser();
      stemwords = stemmer.parseFile(stemmerFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void privateIndex() {
    tokenizer = new PrivateIndexing(stopwords, stemwords);
    tokenizer.putDocuments(documents);
    tokenizer.index(mergeDataPath);
  }

  public void merge() {
    boolean testMode = false; // false=correct, true=testing
    checkTokenizer();
    tokenizer.merge(mergeDataPath, testMode);
  }

  public void mergeSites() {
    boolean testMode = false; // false=correct, true=testing
    checkTokenizer();
    tokenizer.merge(privateMergeDataPath, testMode);
  }

  public void standardStart() {
    this.parseQueries();
    System.out.println("Parsing docs...");
    this.parseTestDocuments();
    System.out.println("Parsing stemming info");
    this.parseStemming();
  }

  public void clear() {
    checkTokenizer();
    tokenizer.clear(mergeDataPath);
    System.out.println("done clearing");
  }

  public void crawl() {
    Crawler crawler = new Crawler();
    try {
      crawler.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void evaluate() {
    Evaluator evaluator = new Evaluator();
    File dir = new File(resultsFiles);
    for (File file : dir.listFiles()) {
      try {
        evaluator.evaluate(qrelFile, file.getName(), true);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void evaluate(String qrelFile, String resultsFile) {
    Evaluator evaluator = new Evaluator();
    try {
      evaluator.evaluate(qrelFile, resultsFile, true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void printFiles() {
    while (true) {
      Scanner in = new Scanner(System.in);
      String fileName = in.nextLine();
      if (fileName.equals("quit")) {
        return;
      }
      System.out.println("looking for: " + fileName);
      String text = documents.get(fileName);
      System.out.println(text);
    }
  }

  public void test() {
    checkTokenizer();
    String testKey = (String) documents.keySet().toArray()[0];
    System.out.println(testKey);
    System.out.println(documents.get(testKey));
    tokenizer.putDocument(testKey, documents.get(testKey));
    tokenizer.index(mergeDataPath);
    try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
              "IndexData/tokenIds.txt"));
      HashMap<Integer, String> list = (HashMap<Integer, String>) ois.readObject();
      ois.close();
      System.out.println(list.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void troubleshootIndex() {
    checkTokenizer();
    DataPrivate data = new DataPrivate(tokenizer);
    data.loadToMemory(new ArrayList<>(documents.keySet()));
    System.out.println("Num Docs- " + documents.size());
//    System.out.println("Vocab Size- " + data.vocabSize());
//    System.out.println("Average Doc Lengths- " + data.avgDocLengths());
//    System.out.println("Total Doc Lengths- " + data.totalDocLengths());
    System.out.println("  this does not do final query scoring, it's mostly for debugging");
    while (true) {
      System.out.println("  enter query");
      Scanner term = new Scanner(System.in);
      String termToCheck = term.nextLine();
      if (termToCheck.equals("quit")) {
        return;
      }
      System.out.println("  now enter document (ID) for tf");
      Scanner docToCheck = new Scanner(System.in);
      String docId = docToCheck.nextLine();
      HashMap<Integer, String> hm = new HashMap<>();
      hm.put(1, termToCheck);
      HashMap<Integer, ArrayList<String>> stemmed = data.getStemmed(hm);
      ArrayList<String> terms = stemmed.get(stemmed.keySet().toArray()[0]);
      data.makeTermsQueryable(terms);
      for (String t : terms) {
//        System.out.println("For term- " + s);
        System.out.println("DF- " + data.df(t));
        System.out.println("TF- " + data.tf(docId, t));
        System.out.println("TF_agg- " + data.tf_agg(t));
        System.out.println("Doc Len- " + data.docLen(docId));
      }
      System.out.println("----------------");
    }
  }

}
