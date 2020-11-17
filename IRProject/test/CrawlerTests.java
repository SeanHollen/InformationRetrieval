import org.junit.Test;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import Crawler.CrawlStorer;
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
      CrawlStorer store = new CrawlStorer();
      store.write("Test/ABC/DEF");
      CrawlStorer store2 = new CrawlStorer();
      store2.isCrawled("Test/ABC/DEF");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void Frontier() {
    String popped;
    Frontier frontier = new Frontier();
    frontier.add("ABC", "");
    frontier.add("Germany", "");
    popped = frontier.pop().getUrl();
    assertEquals("Germany", popped);
    frontier.add("GHI", "");
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
    assertEquals("www.example.com/a.html", canonUrl);
    canonUrl = reader.getCanonicalUrl("http://www.example.com//a.html",
            "http://www.example.com//a.html");
    assertEquals("www.example.com/a.html", canonUrl);
    canonUrl = reader.getCanonicalUrl("HTTP://www.Example.com/SomeFile.html",
            "HTTP://wWw.Example.com/SomeFile.html");
    assertEquals("www.example.com/SomeFile.html", canonUrl);
    canonUrl = reader.getCanonicalUrl("http://www.example.com:80",
            "http://www.example.com:80");
    assertEquals("www.example.com", canonUrl);
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





