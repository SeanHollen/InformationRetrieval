package Indexing;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.*;
import java.io.IOException;
import java.util.HashMap;

public class ElasticIndexing {

  private RestHighLevelClient client;
  private final String stopListPath = "/Users/sean.hollen/Downloads/elasticsearch-7.9.1/config/stoplist.txt";
  private String indexName = "api89";

//  private final String username = "elastic";
//  private final String password = "vNrCFLy9BIyHLCVWFKB50Gpn";
//  private final String cloudId = "temp-deployment:dXMtZWFzdC0xLmF3cy5mb3VuZC5pbyRlYzI3NmU4MGM1YzA0MGQ0YjE" +
//          "5N2M0ZmU4ZThlOTU5YiQ0MGVkYmEyYjE0N2M0MzhjYjcxMjc3MTA3ZTllYWQwMA==";

  public ElasticIndexing(String type) {
    if (type.equals("team")) {
      final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY,
              new UsernamePasswordCredentials("elastic", "aX2WDptDBGznhfh9YShJiKkI"));
      // String host = "40edba2b147c438cb71277107e9ead00.us-east-1.aws.found.io";
      String host = "ec276e80c5c040d4b197c4fe8e8e959b.us-east-1.aws.found.io";
      client = new RestHighLevelClient(RestClient.builder(
              new HttpHost(host, 9243, "https"))
              .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                  return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
              }));
    } else if (type.equals("personal")) {
      try {
        client = new RestHighLevelClient(RestClient.builder(
                new HttpHost("localhost", 9200, "http")));
      } catch (NoClassDefFoundError e) { System.out.println("Not connected"); }
    } else {
      throw new IllegalArgumentException("specify index type");
    }
  }

  public void setIndex(String index) {
    this.indexName = index;
  }

  public void createIndex() throws IOException {
    CreateIndexRequest request = new CreateIndexRequest(indexName);

    XContentBuilder filterSettings = XContentFactory.jsonBuilder();
    filterSettings.startObject();
    {
      filterSettings.startObject("analysis");
      {
        filterSettings.startObject("filter");
        {
          filterSettings.startObject("english_stop");
          {
            filterSettings.field("type", "stop");
            filterSettings.field("stopwords_path", stopListPath); //specified entire path
          }
          filterSettings.endObject();
          filterSettings.startObject("my_stemmer");
          {
            filterSettings.field("type", "stemmer");
            filterSettings.field("name", "english");
          }
          filterSettings.endObject();
        }
        filterSettings.endObject();
        filterSettings.startObject("analyzer");
        {
          filterSettings.startObject("stopped");
          {
            filterSettings.field("type", "custom");
            filterSettings.field("tokenizer", "standard");
            filterSettings.startArray("filter");
            {
              filterSettings.value("lowercase");
              filterSettings.value("english_stop");
              filterSettings.value("my_stemmer");
            }
            filterSettings.endArray();
          }
          filterSettings.endObject();
        }
        filterSettings.endObject();
      }
      filterSettings.endObject();
      filterSettings.field("number_of_shards", 1);
      filterSettings.field("number_of_replicas", 1);
    }
    filterSettings.endObject();
    request.settings(filterSettings);

    XContentBuilder mappingSettings = XContentFactory.jsonBuilder();
    mappingSettings.startObject();
    {
      mappingSettings.startObject("properties");
      {
        mappingSettings.startObject("content");
        {
          mappingSettings.field("type", "text");
          mappingSettings.field("fielddata", true);
          mappingSettings.field("analyzer", "stopped");
          mappingSettings.field("index_options", "positions");
        }
        mappingSettings.endObject();
      }
      mappingSettings.endObject();
    }
    mappingSettings.endObject();
    request.mapping(mappingSettings);

    CreateIndexResponse createIndexResponse = client.indices()
            .create(request, RequestOptions.DEFAULT);
    System.out.println(createIndexResponse.toString());

  }

  public void postDocuments(HashMap<String, String> documents) {
    int docSize = documents.keySet().size();
    System.out.println("indexing " + docSize + " documents");
    System.out.println("with index: " + indexName);
    int counter = 0;
    BulkRequest request = new BulkRequest();
    for (String key : documents.keySet()) {
      if (key.getBytes().length > 512) {
        continue;
      }
      counter++;
      request.add(new IndexRequest(indexName).id(key).source("content", documents.get(key)));
      if (counter % 1000 == 0 || counter == docSize) {
        try {
          BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
          System.out.println(bulkResponse);
        } catch (IOException e) {
          throw new IllegalArgumentException(e);
        }
        System.out.println(counter + " total docs posted");
        // reset
        request = new BulkRequest();
      }
    }
  }

}

