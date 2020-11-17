package Crawler;

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

    for (String seed : seeds) {
      frontier.add(seed, "");
    }
    frontier = new Frontier(visitedLinks.getCrawledLinks());

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
      }
      politeness.reset();
    }
  }

  private void scrapeSite(Link link) throws IOException {
    PrintWriter contentWriter = new PrintWriter("out/CrawledDocuments/" + counter.getCount());
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
    String currentUrl = link.getUrl();
    outlinksWriter.print(link + "\t");
    HashMap<String, String> olMap = parser.getOutLinks();
    for (String newUrl : olMap.keySet()) {
      // todo: use isValid and isHost for something
      newUrl = canonizer.getCanonicalUrl(newUrl, currentUrl);
      if (canonizer.isValid(newUrl))
      frontier.add(newUrl, olMap.get(newUrl));
      outlinksWriter.print(newUrl + "\t");
    }
    outlinksWriter.print("\n");
    counter.docScraped();
  }
}
