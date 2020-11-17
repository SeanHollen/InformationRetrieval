package Crawler;

import java.util.HashMap;

public class PolitenessTracker {

  // HashMap<Host, time>
  private HashMap<String, Long> times;

  public PolitenessTracker() {
    this.times = new HashMap<String, Long>();
  }

  public void waitFor(String host, long currentTime) {
    if (times.containsKey(host)) {
      long diff = currentTime - times.get(host);
      if (diff < 1000) {
        try {
          System.out.println("sleeping: " + (1000 - diff));
          Thread.sleep(1000 - diff);
        } catch (InterruptedException e) { e.printStackTrace(); }
      }
    }
    times.put(host, currentTime);
  }

  public void reset() {
    this.times.clear();
  }

}

