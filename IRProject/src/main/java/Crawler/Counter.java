package Crawler;

public class Counter {

  private int docsScraped;
  private int count;

  public Counter(int start) {
    docsScraped = start;
  }

  public void docScraped() {
    docsScraped++;
    if (docsScraped % 100 == 0) {
      count++;
    }
  }

  public int getCount() {
    return this.count;
  }
}
