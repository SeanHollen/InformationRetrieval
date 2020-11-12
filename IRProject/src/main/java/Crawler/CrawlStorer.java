package Crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

public class CrawlStorer {

  private PrintWriter writer;
  private HashSet<String> crawledLinks;

  // i made changes, check that correct
  public CrawlStorer(String fileName) throws IOException {
    crawledLinks = new HashSet<String>();
    File file = new File(fileName);
    if (!file.exists()) {
      file.createNewFile();
    }
    writer = new PrintWriter(new FileOutputStream(file, true));
    String line;
    BufferedReader reader = new BufferedReader(new FileReader(file));
    while ((line = reader.readLine()) != null) {
      crawledLinks.add(line);
    }
  }

  public boolean isCrawled(String url) {
    return crawledLinks.contains(url);
  }

  public void write(String line) {
    writer.println(line);
    writer.flush();
  }

}
