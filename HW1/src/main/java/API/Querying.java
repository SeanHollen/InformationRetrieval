package main.java.API;

import java.util.HashMap;

public class Querying {

  public void postQueries(HashMap<Integer, String> map) {

  }

  // TODO
  private double ESBuiltIn() {
    return 0;
  }

  private double Okapi_TF(String document, String[] query, double avgDocLen) {
    double sum = 0;
    double docLen = 0; // TODO
    for (String word : query) {
      double tf = 0; // TODO
      sum += tf / (tf + 0.5 + 1.5 * docLen / avgDocLen);
    }
    return sum;
  }

  private double TF_IDF(String document, String[] query, double avgDocLen, double totalDocs) {
    double sum = 0;
    double docLen = 0; // TODO
    for (String word : query) {
      double tf = 0; // TODO
      double okapi_TF = tf / (tf + 0.5 + 1.5 * docLen / avgDocLen);
      sum += okapi_TF + Math.log(totalDocs / df_ofWord(word));
    }
    return sum;
  }

  private double df_ofWord(String word) {
    return 0; // TODO
  }

  private double Okapi_BM25(String document, String[] query, double avgDocLen) {
    double sum = 0;
    double docLen = 0; // TODO
    double D = 0; // what is this? TODO
    double k1 = 0;
    double k2 = 0;
    double b = 0;
    for (String word : query) {
      double tf = 0; // TODO
      double first = Math.log((D + 0.5)/(0.5 + df_ofWord(word)));
      double second_prefix = docLen / avgDocLen;
      double second = (tf + k1 * tf) / (tf + k1 * ((1 - b) + b * second_prefix));
      double third = (tf + k2 * tf) / (tf + k2);
      sum += first * second * third;
    }
    return sum;
  }

  private double Unigram_LM_Laplace(String document, String[] query) {
    double sum = 0;
    double docLen = 0; // TODO
    double V = 0; // TODO
    for (String word : query) {
      double td = 0; // TODO
      sum += (td + 1) / (docLen + V);
    }
    return sum;
  }

  private double Unigram_LM_Jelinek_Mercer() {
    return 0; // TODO
  }

}
