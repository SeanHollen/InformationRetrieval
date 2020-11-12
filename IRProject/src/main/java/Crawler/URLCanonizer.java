package Crawler;

import java.net.URI;
import java.net.URISyntaxException;

public class URLCanonizer {

  public String isHost(String url, String host) {
    try {
      URI uri = new URI(url);
      return uri.getHost() + ".";
    } catch (URISyntaxException e) { throw new IllegalArgumentException(e); }
  }

  public boolean isValid(String url) {
    try {
      URI uri = new URI(url);
      String scheme = uri.getScheme();
      return (scheme.equals("http") || scheme.equals("https") || scheme.equals(""));
    } catch (URISyntaxException e) {
      return false;
    }
  }

  public String getCanonicalUrl(String linkedUrl, String currentURL) {
    if (linkedUrl.contains("#")) {
      linkedUrl = linkedUrl.split("#")[0];
    }
    URI uri;
    try {
      if (isRelativeUrl(linkedUrl)) {
        uri = new URI(parseRelativeUrl(linkedUrl, currentURL));
      } else {
        uri = new URI(linkedUrl);
      }
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    return uri.normalize().toString();
  }

  public boolean isRelativeUrl(String linkedUrl) {
    try {
      return !new URI(linkedUrl).isAbsolute();
    } catch (URISyntaxException e) { throw new IllegalArgumentException(e); }
  }

  public String parseRelativeUrl(String linkedUrl, String currentURL) {
    try {
      URI uri1 = new URI(linkedUrl);
      return uri1.resolve(currentURL).toString();
    } catch (URISyntaxException e) { throw new IllegalArgumentException(e); }
  }

}
