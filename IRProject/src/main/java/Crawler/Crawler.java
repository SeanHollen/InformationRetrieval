package Crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

public class Crawler {

  private CrawlStorer visitedLinks;
  private RobotsReader robots;
  private Frontier frontier;
  private Counter counter;
  private URLCanonizer canonizer;
  private PolitenessTracker politeness;
  private String[] seeds;
  private PrintWriter outlinksWriter;

  public Crawler() {
    try {
      visitedLinks = new CrawlStorer();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      outlinksWriter = new PrintWriter("out/outlinks.txt");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    robots = new RobotsReader();
    canonizer = new URLCanonizer();
    politeness = new PolitenessTracker();
    seeds = new String[]{
            "https://en.wikipedia.org/wiki/Adolf_Hitler%27s_rise_to_power",
            "https://www.history.com/topics/world-war-ii/nazi-party",
            "https://encyclopedia.ushmm.org/content/en/article/the-nazi-rise-to-power",
            "https://www.britannica.com/topic/Nazi-Party"};
    counter = new Counter(1);
  }

  public void setSeeds(String[] seeds) {
    this.seeds = seeds;
  }

  public void start() throws MalformedURLException {

    frontier = new Frontier(visitedLinks.getCrawledLinks());
    for (String seed : seeds) {
      frontier.add(seed, "", 0);
    }

    while (true) {
      System.out.println("give number of docs to crawl");
      Scanner input = new Scanner(System.in);
      String command = input.next();
      int toCrawl;
      try {
        toCrawl = Integer.parseInt(command);
      } catch (NumberFormatException e) {
        System.out.println("Exiting");
        return;
      }
      for (int i = 0; i < toCrawl; i++) {
        Link link = frontier.pop();
        String urlString = link.getUrl();
        URL url = new URL(urlString);
        politeness.waitFor(url.getHost(), System.currentTimeMillis());
        try {
          if (robots.isCrawlingAllowed(urlString)) {
            this.scrapeSite(link);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        visitedLinks.write(urlString);
        System.out.println(urlString + " crawled");
      }
      politeness.reset();
    }
  }

  private void scrapeSite(Link link) throws IOException {
    File file = new File("out/CrawledDocuments/" + counter.getCount() + ".txt");
    if (!file.exists()) {
      file.createNewFile();
      System.out.println("new file created");
    }
    PrintWriter contentWriter = new PrintWriter(file);
    HtmlParser parser = new HtmlParser();
    if (!(parser.getLang() == null) && !parser.getLang().equals("en")) {
      System.out.println("not english");
      return;
    }
    parser.parseContent(link.getUrl());
    contentWriter.println("<DOC>");
    contentWriter.println("<DOCNO>" + link.getUrl() + "</DOCNO>");
    if (parser.getTitle() != null) {
      contentWriter.println("<HEAD>" + parser.getTitle() + "</HEAD>");
    }
    contentWriter.println("<TEXT>");
    for (String s : parser.getContent()) {
      contentWriter.println(s);
    }
    contentWriter.println("</TEXT>");
    contentWriter.println("</DOC>");
    contentWriter.flush();
    String currentUrl = link.getUrl();
    outlinksWriter.print(link + "\t");
    HashMap<String, String> olMap = parser.getOutLinks();
    for (String newUrl : olMap.keySet()) {
      if (canonizer.isValid(newUrl)) {
        String anchorText = olMap.get(newUrl);
        newUrl = canonizer.getCanonicalUrl(newUrl, currentUrl);
        frontier.add(newUrl, anchorText, counter.getDocsScraped());
        outlinksWriter.print(newUrl + "\t");
      }
    }
    outlinksWriter.print("\n");
    outlinksWriter.flush();
    counter.docScraped();
  }
}
