package Ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class LinksGraph {

  public HashMap<String, ArrayList<String>> inlinks;
  public HashMap<String, ArrayList<String>> outlinks;

  public LinksGraph() {
    this.inlinks = new HashMap<>();
    this.outlinks = new HashMap<>();
  }

  public void process(String filePath) throws IOException {
    BufferedReader qrelFileReader = new BufferedReader(new FileReader(new File(filePath)));
    String line;
    while ((line = qrelFileReader.readLine()) != null) {
      String[] split = line.split("\t");
      String firstLink = split[0];
      ArrayList<String> linkedTo = new ArrayList<>();
      for (String to : split) {
        if (!to.equals(firstLink)) {
          linkedTo.add(to);
          if (!inlinks.containsKey(to)) {
            inlinks.put(to, new ArrayList<>());
          }
          inlinks.get(to).add(firstLink);
        }
      }
      outlinks.put(firstLink, linkedTo);
    }
  }

}
