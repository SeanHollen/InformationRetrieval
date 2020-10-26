package Parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class StopwordsParser {

  // HashSet<Term>
  public HashMap<String, String> parseFile(String dir) throws IOException {
    HashMap<String, String> map = new HashMap<String, String>();
    File directory = new File(dir);
    BufferedReader reader = new BufferedReader(new FileReader(directory));
    String line;
    while ((line = reader.readLine()) != null) {
      String[] items = line.split("\\.", 2);
      String root = items[0].trim();
      String[] words = items[1].trim().split("\\|");
      for (String word : words) {
        map.put(word, root);
      }
    }
    return map;
  }
}

