package Crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

public class CrawlStorer {

  private PrintWriter writer;
  private HashSet<String> crawledLinks;

  public CrawlStorer() throws IOException {
    this("out/CrawledDocuments/crawledLinks.txt");
  }

  public CrawlStorer(String path) throws IOException {
    crawledLinks = new HashSet<String>();
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
