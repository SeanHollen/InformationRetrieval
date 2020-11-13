package Crawler;

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
          "hitler", "nazi", "rise", "world war", "Ggrman", "germany", "wwii", "speech", "fascism"};

  public Frontier() {
    this.visited = new HashSet<String>();
    this.frontier = new PriorityQueue<Link>();
    this.linkMap = new HashMap<String, Link>();
    this.waveNumber = 0;

  }

  public String pop() {
    visited.add(currentURL);
    Link link = frontier.poll();
    waveNumber = link.getWaveNumber() + 1;
    currentURL = link.getUrl();
    return currentURL;
  }

  public boolean visited(String url) {
    return visited.contains(url);
  }

  // I made changes, check that correct
  public void addToFrontier(String url) {
    Link link;
    if (!linkMap.containsKey(url)) {
      int keyWordsCount = 0;
      for (String keyword : keywords) {
        if (url.toLowerCase().contains(keyword)) {
          keyWordsCount++;
        }
      }
      link = new Link(url, this.waveNumber, 0, 0, keyWordsCount);
    } else {
      link = linkMap.get(url).clone();
      link.incrementInLinksCount();
    }
    linkMap.put(url, link);
    frontier.add(linkMap.get(url));
  }

  public void removeFromLinkMap(String url) {
    linkMap.remove(url);
  }

  public void updateFrontier() {
    // todo
  }

}







