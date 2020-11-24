package Crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
          "hitler", "nazi", "rise", "power", "world war", "German", "germany", "wwii", "speech",
          "fascism", "world war"};
  private final static String[] bannedAnchorText = new String[]{
          "edit", "sign in", "sign up", "log in", "save", "comment", "delete", "username",
          "password", "account", "tags, signup, login, log-in, sign-up", "Findinalibrarynearyou",
          "archive.org", "find this book"};

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

  public void add(String url, String anchorText, int creationTime, int minKeywords) {
    if (visited.contains(url)) {
      return; // PASS CONDITION
    }
    if (anchorText == null) {
      if (skipNonTextLinks) {
        return; // PASS CONDITION
      } else {
        anchorText = "";
      }
    }
    Link link;
    int keyWordsCount = 0;
    if (!linkMap.containsKey(url)) {
      for (String term : keywords) {
        if (url.toLowerCase().contains(term) || anchorText.toLowerCase().contains(term)) {
          keyWordsCount++;
        }
      }
      for (String term : bannedAnchorText) {
        if (url.toLowerCase().contains(term) || anchorText.toLowerCase().contains(term)) {
          return; // PASS CONDITION
        }
      }
      if (keyWordsCount < minKeywords) {
        return; // PASS CONDITION
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
    File file = new File("out/CrawledDocsMeta/frontier.txt");
    try {
      PrintWriter frontierFile = new PrintWriter(new FileWriter(file, false));
      for (Link link : frontier) {
        frontierFile.print(link + "\t");
      }
      frontierFile.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // todo rewrite this and write() function when I can
  public void read() {
    frontier = new PriorityQueue<Link>();
    File file = new File("out/CrawledDocsMeta/frontier.txt");
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String text = reader.readLine();
      String[] lines = text.split("\\t");
      System.out.println("lines: " + lines.length);
      for (String line : lines) {
        String[] split = line.split(" ");
        String url = split[split.length - 1];
        String anchorText = "";
        for (int i = 0; i < split.length - 1; i++) {
          anchorText += split[i] + " ";
        }
        this.add(url, anchorText, 0, 0);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("priority queue size: " + frontier.size());
  }

}







