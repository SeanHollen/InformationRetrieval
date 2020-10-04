package main.java.Controller;

import java.io.IOException;
import java.util.HashMap;
import main.java.API.Indexing;
import main.java.API.Querying;
import main.java.Indexer.QueryParser;
import main.java.Indexer.TRECparser;

public class Controller {

  Indexing indexing;

  private HashMap<Integer, String> queries = new HashMap<Integer, String>();
  private HashMap<String, String> documents = new HashMap<String, String>();
  private String queryFile = "/Users/sean.hollen/Desktop/IR/CS6200F20/HW1/IR_data/AP_DATA/query_desc.51-100.short.txt";
  private String toParse = "/Users/sean.hollen/Desktop/IR/CS6200F20/HW1/IR_data/AP_DATA/ap89_collection";

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
    indexing.postDocuments(documents);
    System.out.println("posted documents");
  }

  public void query() {
    Querying querying = new Querying();
    querying.postQueries(queries);


  }


}
