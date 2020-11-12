package Crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HtmlContentParser {

  public void parseContent(String url) throws IOException {
    Document doc = Jsoup.connect("https://en.wikipedia.org/").get();
    String title = doc.title();
    // todo search recursively
    Elements content = doc.select("p");
    String element;
    while ((element = content.next().val()) != null) {

    }
  }
}
