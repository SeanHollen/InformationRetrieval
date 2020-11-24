package Indexing;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;

public class ElasticCloudIndexing {

  private RestClient client;

  private final String username = "elastic";
  private final String password = "vNrCFLy9BIyHLCVWFKB50Gpn";
  private final String cloudId = "temp-deployment:dXMtZWFzdC0xLmF3cy5mb3VuZC5pbyRlYzI3NmU4MGM1YzA0MGQ0YjE" +
          "5N2M0ZmU4ZThlOTU5YiQ0MGVkYmEyYjE0N2M0MzhjYjcxMjc3MTA3ZTllYWQwMA==";

  public void cloudIndex() {
    final CredentialsProvider credentialsProvider =
            new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials("user", "password"));
    RestClientBuilder builder = RestClient.builder(
            new HttpHost("localhost", 9200))
            .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
              @Override
              public HttpAsyncClientBuilder customizeHttpClient(
                      HttpAsyncClientBuilder httpClientBuilder) {
                return httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider);
              }
            });
    client = builder.build();
  }

  public void cloudIndex2() {
//    final CredentialsProvider credentialsProvider =
//            new BasicCredentialsProvider();
//    credentialsProvider.setCredentials(AuthScope.ANY,
//            new UsernamePasswordCredentials("<Username>", "<Password>"));
//    client = new RestHighLevelClient(
//            RestClient.builder("<CloudID>"))
//            .setHttpClientConfigCallback(new HttpClientConfigCallback() {
//              @Override
//              public HttpAsyncClientBuilder customizeHttpClient(
//                      HttpAsyncClientBuilder httpClientBuilder) {
//                return httpClientBuilder
//                        .setDefaultCredentialsProvider(credentialsProvider);
//              }
//            });
  }


}
