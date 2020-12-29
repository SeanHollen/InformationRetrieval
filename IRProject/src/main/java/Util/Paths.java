package Util;

public class Paths {

  private static final String prefix = "IRProject/";

  public static final String stoplist = prefix + "/Users/sean.hollen/Downloads/elasticsearch-7.9.1/config/stoplist.txt";
  public static final String queryFile = prefix + "IR_Data/AP_DATA/queries_v4.txt";
  public static final String docsToParse = prefix + "IR_Data/AP_DATA/ap89_collection";
  public static final String sitesToParse = prefix + "out/CrawledDocuments";
  public static final String stopWordsFile = prefix + "IR_Data/AP_DATA/stoplist.txt";
  public static final String stemmerFile = prefix + "IR_Data/AP_DATA/stem-classes.lst";
  public static final String mergeDataPath = prefix + "IndexData";
  public static final String privateMergeDataPath = prefix + "out/CrawledDocuments";
  public static final String qrelFile = prefix + "IR_Data/AP_DATA/qrels.adhoc.51-100.AP89.txt";
  public static final String resultsFiles = prefix + "out/RankingResults";
  public static final String standardQrel = prefix + "IR_Data/AP_DATA/qrels.adhoc.51-100.AP89.txt";
  public static final String esRanking = prefix + "out/RankingResults/es.txt";
  public static final String outlinks = prefix + "out/CrawledDocsMeta/outlinks.txt";
  public static final String crawledDocuments = prefix + "out/CrawledDocuments/";
  public static final String crawledLinks = prefix + "out/CrawledDocsMeta/crawledLinks.txt";
  public static final String frontier = prefix + "out/CrawledDocsMeta/frontier.txt";
  public static final String test = prefix + "out/CrawledDocsMeta/test.txt";
  public static final String indexData = prefix + "IndexData";
  public static final String rankingResults = prefix + "out/RankingResults";
  public static final String featureMatrixArff = prefix + "out/MachineLearning/feature-matrix.arff";
  public static final String featureMatrixTxt = prefix + "out/MachineLearning/feature-matrix.txt";

}
