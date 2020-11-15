import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import Crawler.CrawlStorer;
import Crawler.Frontier;
import Crawler.HtmlParser;
import Crawler.RobotsReader;
import Crawler.URLCanonizer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CrawlerTests {

  @Test
  public void randomAccessFile() {
    String fileText = "abcd\n" +
            "ABCD\n" +
            "1234\n" +
            "!@#%\n";
    String firstFew = "abcd\n" +
            "AB";
    int bitCount = firstFew.getBytes().length;
    File file = new File("aTestFile.txt");
    try {
      RandomAccessFile readFile = new RandomAccessFile(file, "r");
      byte[] bytes = new byte[10];
      readFile.seek(bitCount);
      readFile.read(bytes, 0, 10);
      //String s = sfb.intoString(bytes);
      String s = new String(bytes);
      System.out.println(s);
      assertEquals("CD\n1234\n!@", s);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

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
    frontier.add("ABC");
    frontier.add("Germany");
    popped = frontier.pop();
    assertEquals("Germany", popped);
    frontier.add("GHI");
    popped = frontier.pop();
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
    boolean isRelative = reader.isRelativeUrl(
            "https://encyclopedia.ushmm.org/content/en/article/the-nazi-rise-to-power");
    assertFalse(isRelative);
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
    assertTrue(parser.getOutLinks().contains("https://encyclopedia.ushmm.org/tags/en/tag/aachen"));
    assertEquals(445, parser.getOutLinks().size());
  }

}





