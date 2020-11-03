package Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import API.Indexing;
import API.Querying;
import HW2.Tokenizer;
import Parsers.ParseStopwords;
import Parsers.QueryParser;
import Parsers.StemmerParser;
import Parsers.TRECparser;

public class Controller {

  private Indexing indexing;
  private HashMap<Integer, String> queries = new HashMap<Integer, String>();
  private HashMap<String, String> documents = new HashMap<String, String>();
  HashSet<String> stopwords;
  HashMap<String, String> stemwords;
  Tokenizer tokenizer;
  private String queryFile = "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/IR_Data/AP_DATA/" +
          "queries_v4.txt";
  private String toParse = "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/IR_Data/AP_DATA/" +
          "ap89_collection";
  private String stopWordsFile = "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/IR_Data/" +
          "AP_DATA/stoplist.txt";
  private String stemmerFile = "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/IR_Data/" +
          "AP_DATA/stem-classes.lst";
  private String unmergedData = "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/UnmergedData";

  public void parseQueries() {
    QueryParser queryParser = new QueryParser();
    try {
      queries = queryParser.parseFile(queryFile);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    System.out.println(queries);
  }

  public void parseFiles() {
    TRECparser parser = new TRECparser();
    try {
      documents = parser.parseFiles(toParse);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    System.out.println(documents.keySet());
    // System.out.println("[Example of first listing]: " + documents.get(documents.keySet().toArray()[0]));
  }

  public void createIndex() {
    indexing = new Indexing();
    try {
      indexing.createIndex();
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    System.out.println("created Index");
  }

  public void postFiles() {
    if (indexing == null) {
      indexing = new Indexing();
    }
    indexing.postDocuments(documents);
    System.out.println("posted documents");
  }

  public void query() {
    Querying querying = new Querying();
    querying.queryDocuments(queries, new ArrayList<String>(documents.keySet()));
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
    tokenizer = new Tokenizer(stopwords, stemwords);
    tokenizer.putDocuments(documents);
    tokenizer.index(unmergedData);
  }

  public void merge() {
    tokenizer.merge("/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/UnmergedData");
  }

}
