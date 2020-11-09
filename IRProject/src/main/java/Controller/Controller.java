package Controller;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import API.HW2Data;
import API.Indexing;
import API.Querying;
import HW2.StringFromBytes;
import HW2.Tokenizer;
import Parsers.ParseStopwords;
import Parsers.QueryParser;
import Parsers.StemmerParser;
import Parsers.TRECparser;
import java.io.RandomAccessFile;
public class Controller {

  private Indexing indexing;
  private HashMap<Integer, String> queries = new HashMap<Integer, String>();
  private HashMap<String, String> documents = new HashMap<String, String>();
  private HashSet<String> stopwords;
  private HashMap<String, String> stemwords;
  private Tokenizer tokenizer;
  private String queryFile = "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/IR_Data/AP_DATA/" +
          "queries_v4.txt";
  private String toParse = "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/IR_Data/AP_DATA/" +
          "ap89_collection";
  private String stopWordsFile = "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/IR_Data/" +
          "AP_DATA/stoplist.txt";
  private String stemmerFile = "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/IR_Data/" +
          "AP_DATA/stem-classes.lst";
  private String mergeDataPath = "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/IndexData";

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

  public void createElasticIndex() {
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

  public void queryElastic() {
    Querying querying = new Querying();
    querying.queryDocuments(queries, new ArrayList<String>(documents.keySet()));
  }

  public void queryPrivate() {
    if (tokenizer == null) {
      tokenizer = new Tokenizer(stopwords, stemwords);
    }
    Querying querying = new Querying(new HW2Data(tokenizer));
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
    tokenizer.index(mergeDataPath);
  }

  public void merge() {
    if (tokenizer == null) {
      tokenizer = new Tokenizer(stopwords, stemwords);
    }
    tokenizer.merge(mergeDataPath, false);
  }

  public void standardStart() {
    this.parseQueries();
    System.out.println("Parsing docs...");
    this.parseFiles();
    this.parseStemming();
  }

  public void test() {
    tokenizer = new Tokenizer(stopwords, stemwords);
    String testKey = (String) documents.keySet().toArray()[0];
    System.out.println(testKey);
    System.out.println(documents.get(testKey));
    tokenizer.putDocument(testKey, documents.get(testKey));
    tokenizer.index(mergeDataPath);
    try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
              "/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/IndexData/tokenIds.txt"));
      HashMap<Integer, String> list = (HashMap<Integer, String>) ois.readObject();
      ois.close();
      System.out.println(list.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void testRandomAccessFile() {
    String fileText = "abcd\n" +
            "ABCD\n" +
            "1234\n" +
            "!@#%\n";
    String firstFew = "abcd\n" +
            "AB";
    StringFromBytes sfb = new StringFromBytes();
    int bitCount = firstFew.getBytes().length;
    File file = new File("/Users/sean.hollen/Desktop/IR/CS6200F20/IRProject/aTestFile.txt");
    try {
      RandomAccessFile readFile = new RandomAccessFile(file, "r");
      byte[] bytes = new byte[10];
      readFile.seek(bitCount);
      readFile.read(bytes, 0, 10);
      String s = sfb.intoString(bytes);
      System.out.println(s);
      assertEquals("CD\n1234\n!@", s);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void clear() {
    if (tokenizer == null) {
      tokenizer = new Tokenizer(stopwords, stemwords);
    }
    tokenizer.clear(mergeDataPath);
  }

}
