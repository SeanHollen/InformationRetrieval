import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import Crawler.Logger;
import Crawler.Frontier;
import Crawler.HtmlParser;
import Crawler.RobotsReader;
import Crawler.URLCanonizer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CrawlerTests {

  @Test
  public void crawlStore() {
    try {
      Logger store = new Logger();
      store.write("Test/ABC/DEF");
      Logger store2 = new Logger();
      store2.isCrawled("Test/ABC/DEF");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void Frontier() {
    String popped;
    Frontier frontier = new Frontier();
    frontier.add("ABC", "", 0);
    frontier.add("Germany", "", 0);
    popped = frontier.pop().getUrl();
    assertEquals("Germany", popped);
    frontier.add("GHI", "", 0);
    popped = frontier.pop().getUrl();
    assertEquals("ABC", popped);
  }

  @Test
  public void htmlParser() {
    HtmlParser parser = new HtmlParser();
  }

  @Test
  public void robotsReader() {
    RobotsReader reader = new RobotsReader();
    boolean allowed;
    try {
      allowed = reader.isCrawlingAllowed(
              "https://encyclopedia.ushmm.org/content/en/article/the-nazi-rise-to-power");
      assertTrue(allowed);
    } catch (IOException e) { throw new IllegalArgumentException(e); }
  }

  @Test
  public void urlCanonizer() {
    URLCanonizer reader = new URLCanonizer();
    boolean isValid = reader.isValid("https://encyclopedia.ushmm.org/content/en/article/the-nazi-rise-to-power");
    assertTrue(isValid);
    try {
      URI uri = new URI("https://encyclopedia.ushmm.org/content/en/article/the-nazi-rise-to-power");
      System.out.println("Host: " + uri.getHost());
      assertTrue(reader.isHost("https://encyclopedia.ushmm.org/content/en/article/the-nazi-rise-to-power", uri.getHost()));
    } catch (URISyntaxException e) { e.printStackTrace(); }
    String canonUrl = reader.getCanonicalUrl(
            "/narrative/11997/en",
            "https://encyclopedia.ushmm.org/content/en/article/the-nazi-rise-to-power");
    assertEquals("https://encyclopedia.ushmm.org/narrative/11997/en", canonUrl);
  }

  @Test
  public void urlCanonizer2() {
    URLCanonizer reader = new URLCanonizer();
    String canonUrl;
    canonUrl = reader.getCanonicalUrl("http://www.example.com/a.html#anything",
            "http://www.example.com/a.html#anything");
    assertEquals("http://www.example.com/a.html", canonUrl);
    canonUrl = reader.getCanonicalUrl("http://www.example.com//a.html",
            "http://www.example.com//a.html");
    assertEquals("http://www.example.com/a.html", canonUrl);
    canonUrl = reader.getCanonicalUrl("HTTP://www.Example.com/SomeFile.html",
            "HTTP://wWw.Example.com/SomeFile.html");
    assertEquals("http://www.example.com/SomeFile.html", canonUrl);
    canonUrl = reader.getCanonicalUrl("http://www.example.com:80",
            "http://www.example.com:80");
    assertEquals("http://www.example.com", canonUrl);
    canonUrl = reader.getCanonicalUrl("https://www.historylearningsite.co.uk/WORLD WAR TWO.htm",
            "https://www.historylearningsite.co.uk/WORLD WAR TWO.htm");
    assertEquals("https://www.historylearningsite.co.uk/WORLD-WAR-TWO.htm", canonUrl);
  }

  @Test
  public void misc() {
    try {
      URI uria = new URI("https://www.historylearningsite.co.uk/WORLD%20WAR%20TWO.htm");
      URI urib = new URI(uria.getScheme().toLowerCase() + "://" + uria.getHost().toLowerCase()
              + uria.getPath().replaceAll(" ", "-"));
      System.out.println(urib.getPath());

      URI uri = new URI("https://encyclopedia.ushmm.org/content/en/article/the-nazi-rise-to-power");
      System.out.println("port: " + uri.getHost());
    } catch (URISyntaxException e) { e.printStackTrace(); }
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) { e.printStackTrace(); }
  }

  @Test
  public void HtmlParser() {
    HtmlParser parser = new HtmlParser();
    try {
      parser.parseContent("https://encyclopedia.ushmm.org/content/en/article/the-great-depression");
    } catch (IOException e) {
      e.printStackTrace();
    }
    assertEquals("The Great Depression | The Holocaust Encyclopedia", parser.getTitle());
    assertTrue(parser.getContent().contains("Within the United States, the repercussions of the " +
            "crash reinforced and even strengthened the existing restrictive American immigration policy."));
    assertEquals(29, parser.getContent().size());
    assertTrue(parser.getOutLinks().containsKey("https://encyclopedia.ushmm.org/tags/en/tag/aachen"));
    assertEquals(440, parser.getOutLinks().size());
  }

}





