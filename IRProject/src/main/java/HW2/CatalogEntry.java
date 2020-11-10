package HW2;

public class CatalogEntry {

  public int tokenHash;
  public String token;
  public int startPlace;
  public int endPlace;

  public CatalogEntry(int tokenHash, String token, int startPlace, int endPlace) {
    this.tokenHash = tokenHash;
    this.token = token;
    this.startPlace = startPlace;
    this.endPlace = endPlace;
  }
}
