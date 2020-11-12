package Crawler;

public class Link {

  private String url;
  private int waveNumber;
  private int inLinkCount;
  private int arrivalTime;
  private int keyWordsCount;

  public Link(String url, int waveNumber, int inLinkCount, int arrivalTime, int keyWordsCount) {
    this.url = url;
    this.waveNumber = waveNumber;
    this.inLinkCount = inLinkCount;
    this.arrivalTime = arrivalTime;
    this.keyWordsCount = keyWordsCount;
  }

  public Link clone() {
    return new Link(this.url, this.waveNumber, this.inLinkCount, this.arrivalTime, this.keyWordsCount);
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

  public int getArrivalTime() {
    return arrivalTime;
  }

}
