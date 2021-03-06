package Parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class ParseStopwords {

  // Map<QueryNo, Query>
  public HashSet<String> parseFile(String dir) throws IOException {
    HashSet<String> map = new HashSet<>();
    File directory = new File(dir);
    BufferedReader reader = new BufferedReader(new FileReader(directory));
    String line;
    while ((line = reader.readLine()) != null) {
      map.add(line);
    }
    return map;
  }
}
