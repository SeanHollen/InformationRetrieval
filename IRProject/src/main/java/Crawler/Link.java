package Crawler;

public class Link implements Comparable<Link> {

  private String url;
  private int waveNumber;
  private int inLinkCount;
  private int creationTime;
  private int keyWordsCount;
  private boolean deleted;

  public Link(String url, int waveNumber, int inLinkCount, int arrivalTime, int keyWordsCount) {
    this.url = url;
    this.waveNumber = waveNumber;
    this.inLinkCount = inLinkCount;
    this.creationTime = arrivalTime;
    this.keyWordsCount = keyWordsCount;
    this.deleted = false;
  }

  public Link clone() {
    return new Link(this.url, this.waveNumber, this.inLinkCount, this.creationTime, this.keyWordsCount);
  }

  public String getUrl() {
    return this.url;
  }

  public int getWaveNumber() {
    return this.waveNumber;
  }

  public int getInLinkCount() {
    return inLinkCount;
  }

  public void incrementInLinksCount() {
    inLinkCount++;
  }

  public void incrementKeywordsCount(int n) {
    keyWordsCount += n;
  }

  public int getCreationTime() {
    return creationTime;
  }

  public int compareTo(Link other) {
    if (this.waveNumber > other.waveNumber) {
      return 1;
    } else if (this.waveNumber < other.waveNumber) {
      return -1;
    }
    if (this.inLinkCount == other.inLinkCount) {
      return other.keyWordsCount - this.keyWordsCount;
    } else {
      return other.inLinkCount - this.inLinkCount;
    }
  }

}
