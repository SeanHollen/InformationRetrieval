package Crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class CrawlerRunner {

  private Counter counter;
  private CrawlStorer store;
  private RobotsReader robots;
  private Frontier frontier;
  private HtmlParser htmlParser;
  private URLCanonizer urlCanonizer;
  private String[] seeds = new String[]{
          "https://en.wikipedia.org/wiki/Adolf_Hitler%27s_rise_to_power",
          "https://www.history.com/topics/world-war-ii/nazi-party",
          "https://encyclopedia.ushmm.org/content/en/article/the-nazi-rise-to-power",
          "https://www.britannica.com/topic/Nazi-Party"};

  public CrawlerRunner() {
    this.counter = new Counter();
    try {
      this.store = new CrawlStorer();
    } catch (IOException e) {
      e.printStackTrace();
    }
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

    for (int i = 0; i < 10; i++) {

    }
    // todo
  }
}
