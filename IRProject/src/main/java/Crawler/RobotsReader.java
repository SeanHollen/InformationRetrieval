package Crawler;

import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpGet;
import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;

public class RobotsReader {

  private HashMap<String, BaseRobotRules> robotsTextMap;
  final String userAgent = "crawlerbot";

  public RobotsReader() {
    robotsTextMap = new HashMap<>();
  }

  public boolean isCrawlingAllowed(String urlString) throws IOException {
    try {
      URL url = new URL(urlString);
      String hostId = url.getProtocol() + "://" + url.getHost();
      if (url.getPort() > -1) {
        hostId += url.getPort();
      }
      if (!robotsTextMap.containsKey(hostId)) {
        addToRobotsTextMap(hostId);
      }
      BaseRobotRules rules = robotsTextMap.get(hostId);
      return rules.isAllowed(urlString) || !rules.isAllowNone();
    } catch (Exception e) {
      e.printStackTrace();
      return false; // PASS CONDITION
    }
  }

  private void addToRobotsTextMap(String hostId) throws Exception {
    BaseRobotRules rules;
    CloseableHttpClient client = HttpClients.createDefault();
    HttpResponse response = client.execute(new HttpGet(hostId + "/robots.txt"),
            new BasicHttpContext());
    if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == 404) {
      rules = new SimpleRobotRules(SimpleRobotRules.RobotRulesMode.ALLOW_ALL);
      EntityUtils.consumeQuietly(response.getEntity());
    } else {
      BufferedHttpEntity entity = new BufferedHttpEntity(response.getEntity());
      SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
      rules = robotParser.parseContent(hostId, IOUtils.toByteArray(entity.getContent()),
              "text/plain", userAgent);
    }
    robotsTextMap.put(hostId, rules);
  }

}
