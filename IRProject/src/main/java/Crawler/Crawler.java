package Crawler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Crawler {

  private CrawlStorer store;
  private Frontier frontier;
  private RobotsReader robots;
  private int counter;
  private String[] seeds = new String[]{
          "https://en.wikipedia.org/wiki/Adolf_Hitler%27s_rise_to_power",
          "https://www.history.com/topics/world-war-ii/nazi-party",
          "https://encyclopedia.ushmm.org/content/en/article/the-nazi-rise-to-power",
          "https://www.britannica.com/topic/Nazi-Party"};
  HashSet<String> crawled;

  public Crawler() {
    try {
      this.store = new CrawlStorer();
    } catch (IOException e) { e.printStackTrace(); }
    // todo read from store
  }

  private void start() throws MalformedURLException {

    HashMap<String, ArrayList<String>> seedsMap = new HashMap<String, ArrayList<String>>();

    for (String seed : seeds) {
      URL url = new URL(seed);
      if (seedsMap.containsKey(url.getHost())) {
        seedsMap.get(url.getHost()).add(seed);
      } else {
        ArrayList<String> urlList = new ArrayList<String>();
        urlList.add(seed);
        seedsMap.put(url.getHost(), urlList);
      }
    }
    // todo
    // Store passes crawled links to frontier
    // Add seeds to frontier
    // Loop through documents in frontier
    // Crawl documents 10 at a time. delay 1 second between documents with the same host
    // Canonize the URL
    // Check if crawling allowed from RobotsReader
    // Make sure I store links
  }

  public void createDocument(Link link) throws IOException {
    PrintWriter writer = new PrintWriter("out/CrawledDocuments/" + counter);
    writer.println("<DOC>");
    writer.println("<DOCNO>" + link.getUrl() + "</DOCNO>");
    writer.println("<TEXT>");
    HtmlParser parser = new HtmlParser();
    parser.parseContent(link.getUrl()); // todo organization
    for (String s : parser.getContent()) {
      writer.println(s);
    }
    writer.println("</TEXT>");
    writer.println("</DOC>");
    for (String s : parser.getOutLinks()) {
      frontier.add(s);
    }
    counter++;
  }
}
