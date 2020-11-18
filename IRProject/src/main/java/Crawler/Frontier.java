package Crawler;

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
  private final static String[] keywords = new String[]{
          "hitler", "nazi", "rise", "world war", "German", "germany", "wwii", "speech", "fascism"};

  public Frontier() {
    this.visited = new HashSet<String>();
    this.frontier = new PriorityQueue<Link>();
    this.linkMap = new HashMap<String, Link>();
    this.waveNumber = 0;
  }

  public Frontier(HashSet<String> visited) {
    this.visited = visited;
    this.frontier = new PriorityQueue<Link>();
    this.linkMap = new HashMap<String, Link>();
    this.waveNumber = 0;
  }

  public Link pop() {
    //System.out.println(Arrays.toString(frontier.toArray()));
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
    //System.out.println("adding " + url);
    if (anchorText == null) {
      anchorText = "";
    }
    Link link;
    int keyWordsCount = 0;
    if (!linkMap.containsKey(url)) {
      for (String keyword : keywords) {
        if (url.toLowerCase().contains(keyword) || anchorText.toLowerCase().contains(keyword)) {
          keyWordsCount++;
        }
      }
      link = new Link(url, this.waveNumber, 1, creationTime, keyWordsCount);
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

}







