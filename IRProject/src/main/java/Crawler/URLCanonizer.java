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
      return (scheme.equals("http") || scheme.equals("https") || scheme.equals(""));
    } catch (URISyntaxException e) {
      return false;
    }
  }

  public String getCanonicalUrl(String linkedUrl, String currentURL) {
    if (linkedUrl.contains("#")) {
      linkedUrl = linkedUrl.split("#")[0];
    }
    if (linkedUrl.split("//")[1].contains("//")) {
      linkedUrl = linkedUrl.split("//")[0];
    }
    URI uri;
    try {
      if (isRelativeUrl(linkedUrl)) {
        uri = new URI(parseRelativeUrl(linkedUrl, currentURL));
      } else {
        uri = new URI(linkedUrl);
      }
      if (uri.getPath().contains("//")) {
        String[] split = uri.getPath().split("//");
        if (split.length > 2) {
          throw new IllegalArgumentException("weird url: " + uri.toString());
        }
        uri = new URI(uri.getHost().toLowerCase() + split[0] + "/" + split[1]);
      } else {
        uri = new URI(uri.getHost().toLowerCase() + uri.getPath());
      }
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    try {
      return uri.normalize().toString();
    } catch (Exception e) {
      throw new IllegalArgumentException("malformed");
    }
  }

  private boolean isRelativeUrl(String linkedUrl) {
    try {
      return !new URI(linkedUrl).isAbsolute();
    } catch (URISyntaxException e) { throw new IllegalArgumentException(e); }
  }

  private String parseRelativeUrl(String linkedUrl, String currentURL) {
    try {
      URI uri = new URI(currentURL);
      return uri.resolve(linkedUrl).toString();
    } catch (URISyntaxException e) { throw new IllegalArgumentException(e); }
  }

}
