package API;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import java.io.IOException;
import java.util.HashMap;

public class Indexing {

  private RestHighLevelClient client;
  private String stopListPath = "/Users/sean.hollen/Downloads/elasticsearch-7.9.1/config/stoplist.txt";

  public Indexing() {
    client = new RestHighLevelClient(RestClient.builder(
            new HttpHost("localhost", 9200, "http")));
  }

  public void createIndex() throws IOException {

    CreateIndexRequest request = new CreateIndexRequest("api89");

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

    CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
    System.out.println(createIndexResponse.toString());

  }

  public void postDocuments(HashMap<String, String> documents) {
    System.out.println("indexing " + documents.keySet().size() + " documents");
    int counter = 0;
    BulkRequest request = new BulkRequest();
    for (String key : documents.keySet()) {
      counter++;
      request.add(new IndexRequest("api89").id(key)
              .source("content", documents.get(key)));
      if (counter == 1000) {
        try {
          BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
          System.out.println(bulkResponse);
        } catch (IOException e) {
          throw new IllegalArgumentException(e);
        }
        // reset
        request = new BulkRequest();
        counter = 0;
      }
    }
  }

}
