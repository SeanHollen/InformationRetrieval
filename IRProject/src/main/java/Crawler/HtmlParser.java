package Crawler;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HtmlParser {

  private String title;
  private ArrayList<String> content;
  // map<link, anchor text>
  private HashMap<String, String> outLinks;
  private String lang;

  public HtmlParser() {
    this.content = new ArrayList<String>();
    this.outLinks = new HashMap<String, String>();
  }

  public boolean parseContent(String url) throws IOException {
    Document doc;
    Connection connection = Jsoup.connect(url);
    Connection.Response response = connection.response();
    int code = response.statusCode();
    if (code != 0) {
      System.out.println("Got response " + code);
      return false; // PASS CONDITION
    }
    try {
      try {
        doc = connection.get();
      } catch (UnsupportedMimeTypeException e) {
        System.out.println("unsupported mime type: " + url);
        return false; // PASS CONDITION
      }
    } catch (HttpStatusException e) {
      System.out.println("http status exception: " + url);
      return false; // PASS CONDITION
    }
    title = doc.title();
    lang = doc.select("html").attr("lang");
    content.add(title);
    Elements links = doc.select("a[href]");
    for (Element link : links) {
      String attr = link.attr("href");
      String anchor = link.text();
      if (!attr.equals("#") && !attr.equals("") && !outLinks.containsKey(attr)) {
        outLinks.put(attr, anchor);
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
        return true;
      }
      i++;
    }
    return true;
  }

  public String getTitle() {
    return this.title;
  }

  public HashMap<String, String> getOutLinks() {
    return this.outLinks;
  }

  public ArrayList<String> getContent() {
    return this.content;
  }

  public String getLang() {
    return lang;
  }
}
