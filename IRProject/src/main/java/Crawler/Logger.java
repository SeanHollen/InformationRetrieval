package Crawler;

import java.io.*;
import java.util.HashSet;

import Util.Paths;

public class Logger {

  private PrintWriter writer;
  private HashSet<String> crawledLinks;

  public Logger() throws IOException {
    this(Paths.crawledLinks);
  }

  public Logger(String path) throws IOException {
    crawledLinks = new HashSet<>();
    File file = new File(path);
    if (!file.exists()) {
      file.createNewFile();
    }
    writer = new PrintWriter(new FileWriter(file, true));
    String line;
    BufferedReader reader = new BufferedReader(new FileReader(file));
    while ((line = reader.readLine()) != null) {
      crawledLinks.add(line);
    }
  }

  public boolean isCrawled(String url) {
    return crawledLinks.contains(url);
  }

  public HashSet<String> getCrawledLinks() {
    return crawledLinks;
  }

  public void write(String link) {
    crawledLinks.add(link);
    writer.println(link);
    writer.flush();
  }

}
