package Crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Frontier {

  private HashSet<String> visited;
  private PriorityQueue<Link> frontier;
  private HashMap<String, Link> linkMap;
  private int waveNumber;
  private String currentURL;
  private boolean skipNonTextLinks;
  private final static String[] keywords = new String[]{
          "hitler", "nazi", "rise", "world war", "German", "germany", "wwii", "speech", "fascism"};
  private final static String[] bannedAnchorText = new String[]{
          "edit", "sign in", "sign up", "log in", "save", "comment", "delete", "username", "password", "account"};

  public Frontier() {
    this.visited = new HashSet<String>();
    this.frontier = new PriorityQueue<Link>();
    this.linkMap = new HashMap<String, Link>();
    this.waveNumber = 0;
  }

  public Frontier(HashSet<String> visited, boolean skipNonTextLinks) {
    this.visited = visited;
    this.frontier = new PriorityQueue<Link>();
    this.linkMap = new HashMap<String, Link>();
    this.waveNumber = 0;
    this.skipNonTextLinks = skipNonTextLinks;
  }

  public Link pop() {
    visited.add(currentURL);
    Link link = frontier.poll();
    waveNumber = link.getWaveNumber() + 1;
    currentURL = link.getUrl();
    return link;
  }

  public boolean wasVisited(String url) {
    return visited.contains(url);
  }

  public void add(String url, String anchorText, int creationTime) {
    if (anchorText == null) {
      if (skipNonTextLinks) {
        return; // STOP CONDITION
      } else {
        anchorText = "";
      }
    }
    Link link;
    int keyWordsCount = 0;
    if (!linkMap.containsKey(url)) {
      for (String keyword : keywords) {
        if (url.toLowerCase().contains(keyword) || anchorText.toLowerCase().contains(keyword)) {
          keyWordsCount++;
        }
      }
      for (String bannedWord : bannedAnchorText) {
        if (anchorText.contains(bannedWord)) {
          return; // STOP CONDITION
        }
      }
      link = new Link(url, anchorText, this.waveNumber, 1, creationTime, keyWordsCount);
      linkMap.put(url, link);
      frontier.add(link);
    } else {
      for (String keyword : keywords) {
        if (anchorText.toLowerCase().contains(keyword)) {
          keyWordsCount++;
        }
      }
      link = linkMap.get(url);
      link.incrementInLinksCount();
      link.incrementKeywordsCount(keyWordsCount);
    }
  }

  public void removeFromLinkMap(String url) {
    linkMap.remove(url);
  }

  public void print() {
    System.out.println(Arrays.toString(frontier.toArray()));
  }

  public void write() {
    File file = new File("out/CrawledDocuments/frontier.txt");
    try {
      PrintWriter frontierFile = new PrintWriter(new FileWriter(file, true));
      frontierFile.print("\n");
      for (Link link : frontier) {
        frontierFile.print(link + "\t");
      }
      frontierFile.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}







