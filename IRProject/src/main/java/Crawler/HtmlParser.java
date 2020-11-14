package Crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class HtmlParser {

  private String title;
  private ArrayList<String> outLinks;
  private ArrayList<String> content;

  public HtmlParser() {
    this.outLinks = new ArrayList<String>();
    this.content = new ArrayList<String>();
  }

  public void parseContent(String url) throws IOException {
    Document doc = Jsoup.connect(url).get();
    title = doc.title();
    Elements links = doc.select("a[href]");
    for (Element link : links) {
      String attr = link.attr("href");
      if (!attr.equals("#") && !attr.equals("")) {
        outLinks.add(attr);
      }
    }
    Elements paragraphs = doc.select("p");
    int i = 0;
    for (Element paragraph : paragraphs) {
      String val = paragraph.text();
      if (!val.equals("")) {
        content.add(val);
      }
      if (i > 1000) {
        return;
      }
      i++;
    }
  }

  public String getTitle() {
    return this.title;
  }

  public ArrayList<String> getOutLinks() {
    return this.outLinks;
  }

  public ArrayList<String> getContent() {
    return this.content;
  }
}
