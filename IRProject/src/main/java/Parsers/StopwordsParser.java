package Parsers;

import weka.core.stemmers.SnowballStemmer;

import weka.classifiers.Classifier;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class StopwordsParser {

  // HashSet<Term>
  public HashSet<String> parseFile(String dir) throws IOException {

    SnowballStemmer stemmer = new SnowballStemmer();
    stemmer.setStemmer("english");

      HashSet<String> map = new HashSet<String>();
//    File directory = new File(dir);
//    BufferedReader reader = new BufferedReader(new FileReader(directory));
//    String line;
//    while ((line = reader.readLine()) != null) {
//      map.add(line);
//    }
      return map;
  }
}
