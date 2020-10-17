package Parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class QueryParser {

  // Map<QueryNo, Query>
  public HashMap<Integer, String> parseFile(String dir) throws IOException {
    HashMap<Integer, String> map = new HashMap<Integer, String>();
    File directory = new File(dir);
    BufferedReader reader = new BufferedReader(new FileReader(directory));
    String line;
    while ((line = reader.readLine()) != null) {
      String[] splitStrings = line.split("\\.", 2);
      if (splitStrings.length < 2) {
        continue;
      }
      map.put(Integer.parseInt(splitStrings[0]), splitStrings[1].trim());
    }
    return map;
  }

}
