package Crawler;

import com.sun.tools.internal.ws.wsdl.document.http.HTTPConstants;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpGet;
import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;

// 1:10:46 https://northeastern.hosted.panopto.com/Panopto/Pages/Viewer.aspx?id=696a45cd-3880-449c-af2b-ac62014ee8bb
public class RobotsReader {

  private HashMap<String, BaseRobotRules> robotsTextMap;

  public RobotsReader() {
    robotsTextMap = new HashMap<String, BaseRobotRules>();
  }

  public boolean isCrawlingAllowed(String urlString) throws IOException {
    // i deleted the check of url == "deleted" 1:10:38
    // todo understand what this does
    final String userAgent = "crawlerbot";
    URL urlObject;
    urlObject = new URL(urlString);
    String hostId = urlObject.getProtocol() + "://" + urlObject.getHost();
    if (urlObject.getPort() > -1) {
      hostId += urlObject.getPort();
    }
    BaseRobotRules rules = robotsTextMap.get(hostId);
    if (rules == null) {
      HttpGet httpget = new HttpGet(hostId + "/robots.txt");
      HttpContext context = new BasicHttpContext();
      CloseableHttpClient client = HttpClients.createDefault();
      HttpResponse response = null;
      response = client.execute(httpget, context);
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
    return rules.isAllowed(urlString) || !rules.isAllowNone();
  }
}
