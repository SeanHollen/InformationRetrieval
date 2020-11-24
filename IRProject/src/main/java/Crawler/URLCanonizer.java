package Crawler;

import java.net.URI;
import java.net.URISyntaxException;

public class URLCanonizer {

  public boolean isHost(String url, String host) {
    try {
      URI uri = new URI(url);
      return uri.getHost().equals(host);
    } catch (URISyntaxException e) { throw new IllegalArgumentException(e); }
  }

  public boolean isValid(String url) {
    try {
      URI uri = new URI(url);
      String scheme = uri.getScheme();
      return scheme != null && (scheme.equals("http") || scheme.equals("https") || scheme.equals(""));
    } catch (URISyntaxException e) {
      return false;
    }
  }

  public String getCanonicalUrl(String linkedUrl, String currentURL) {
    if (linkedUrl.contains("#")) {
      linkedUrl = linkedUrl.split("#")[0];
    }
    URI uri;
    linkedUrl = linkedUrl.replaceAll(" ", "-");
    try {
      if (isRelativeUrl(linkedUrl)) {
        uri = new URI(parseRelativeUrl(linkedUrl, currentURL));
      } else {
        uri = new URI(linkedUrl);
      }
      if (uri.getPath().contains("//")) {
        String newPath = uri.getPath().replaceAll("//", "/");
        uri = new URI(uri.getScheme().toLowerCase() + "://" + uri.getHost().toLowerCase() + newPath);
      } else {
        uri = new URI(uri.getScheme().toLowerCase() + "://" + uri.getHost().toLowerCase()
                + uri.getPath().replaceAll(" ", "-"));
      }
    } catch (Exception e) {
      System.out.println("bad syntax: " + linkedUrl);
      return ""; // PASS CONDITION
    }
    return uri.normalize().toString();
  }

  private boolean isRelativeUrl(String linkedUrl) throws URISyntaxException {
    return !(new URI(linkedUrl).isAbsolute());
  }

  private String parseRelativeUrl(String linkedUrl, String currentURL) {
    try {
      URI uri = new URI(currentURL);
      return uri.resolve(linkedUrl).toString();
    } catch (URISyntaxException e) { throw new IllegalArgumentException(e); }
  }

}
