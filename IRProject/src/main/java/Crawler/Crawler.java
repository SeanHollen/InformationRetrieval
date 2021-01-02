package Crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

import Util.Paths;

public class Crawler {

  private Logger visitedLinks;
  private RobotsReader robots;
  private Frontier frontier;
  private Counter counter;
  private URLCanonizer canonizer;
  private PolitenessTracker politeness;
  private String[] seeds;
  private PrintWriter outlinksWriter;

  public Crawler() {
    try {
      visitedLinks = new Logger();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      File file = new File(Paths.outlinks);
      outlinksWriter = new PrintWriter(new FileWriter(file, true));
    } catch (IOException e) {
      e.printStackTrace();
    }
    robots = new RobotsReader();
    canonizer = new URLCanonizer();
    politeness = new PolitenessTracker();
    // starting seeds
    seeds = new String[]{
            "https://en.wikipedia.org/wiki/Adolf_Hitler%27s_rise_to_power",
            "https://www.history.com/topics/world-war-ii/nazi-party",
            "https://encyclopedia.ushmm.org/content/en/article/the-nazi-rise-to-power",
            "https://www.britannica.com/topic/Nazi-Party"};
    int counterStart = new File("out/CrawledDocuments/").listFiles().length + 1;
    counter = new Counter(counterStart);
  }

  public void setSeeds(String[] seeds) {
    this.seeds = seeds;
  }

  public void start() throws MalformedURLException {

    frontier = new Frontier(visitedLinks.getCrawledLinks(), true);
    for (String seed : seeds) {
      frontier.add(seed, "", 0, 0);
    }

    while (true) {
      System.out.println("give number of docs to crawl (or deleteAll, printFrontier, writeFrontier, readFrontier)");
      Scanner input = new Scanner(System.in);
      String command = input.next();
      int toCrawl;
      try {
        toCrawl = Integer.parseInt(command);
      } catch (NumberFormatException e) {
        readCommand(command);
        continue;
      }
      crawl500Pages(toCrawl);
      politeness.reset();
    }
  }

  private void readCommand(String command) {
    if (command.equals("deleteAll") || command.equals("delete")) {
      deleteAll();
    } else if (command.equals("printFrontier") || command.equals("print")) {
      frontier.print();
    } else if (command.equals("writeFrontier") || command.equals("write")) {
      frontier.write();
    } else if (command.equals("readFrontier") || command.equals("read")) {
      frontier.read();
    } else if (command.equals("quit")) {
      return;
    }
  }

  private void crawl500Pages(int toCrawl) throws MalformedURLException {
    for (int i = 1; i <= toCrawl; i++) {
      Link link = frontier.pop();
      System.out.println(link.toString() + " to crawl");
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
      if (i % 500 == 0) {
        System.out.println("writing frontier to file");
        frontier.write();
      }
    }
  }

  private void scrapeSite(Link link) throws IOException {
    File file = new File(Paths.crawledDocuments + counter.getCount() + ".txt");
    if (!file.exists()) {
      file.createNewFile();
      System.out.println("new file created");
    }
    PrintWriter contentWriter = new PrintWriter(new FileWriter(file, true));
    String currentUrl = link.getUrl();
    HtmlParser parser = new HtmlParser();
    boolean success = parser.parseContent(currentUrl);
    if (!success) {
      System.out.println("not parsable");
      return;
    }
    if (parser.getLang() == null || !parser.getLang().equals("en")) {
      System.out.println("not english");
      return;
    }
    contentWriter.println("<DOC>");
    contentWriter.println("<DOCNO>" + currentUrl + "</DOCNO>");
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
    outlinksWriter.print(currentUrl + "\t");
    HashMap<String, String> olMap = parser.getOutLinks();
    for (String newUrl : olMap.keySet()) {
      if (canonizer.isValid(newUrl)) {
        String anchorText = olMap.get(newUrl);
        newUrl = canonizer.getCanonicalUrl(newUrl, currentUrl);
        if (newUrl.equals("")) {
          continue;
        }
        outlinksWriter.print(newUrl + "\t");
        frontier.add(newUrl, anchorText, counter.getDocsScraped(), 0);
      }
    }
    outlinksWriter.print("\n");
    outlinksWriter.flush();
    counter.docScraped();
  }

  private void deleteAll() {
    System.out.println("Type \"delete\" to confirm");
    Scanner input = new Scanner(System.in);
    String command = input.next();
    if (!command.equals("delete")) {
      return;
    }
    File dir1 = new File("out/CrawledDocuments/");
    for (File doc : dir1.listFiles()) {
      doc.delete();
    }
    File dir2 = new File("out/CrawledDocsMeta/");
    for (File doc : dir2.listFiles()) {
      doc.delete();
    }
    try {
      File outlinks = new File(Paths.outlinks);
      outlinks.createNewFile();
      File crawledlinks = new File(Paths.crawledLinks);
      crawledlinks.createNewFile();
      File frontier = new File(Paths.frontier);
      frontier.createNewFile();
      File testFile = new File(Paths.test);
      testFile.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      visitedLinks = new Logger();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      File file = new File(Paths.outlinks);
      outlinksWriter = new PrintWriter(new FileWriter(file, true));
    } catch (IOException e) {
      e.printStackTrace();
    }
    counter = new Counter(1);
  }
}
