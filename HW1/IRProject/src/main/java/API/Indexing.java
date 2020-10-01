package API;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.HashMap;

public class Indexing {

  private RestHighLevelClient restClient;

  public void createIndex() throws IOException {

    restClient = new RestHighLevelClient(RestClient.builder(
            new HttpHost("localhost", 9200, "http")));

    CreateIndexRequest request = new CreateIndexRequest("API89");

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
            filterSettings.field("stopwords_path", "my_stoplist.txt");
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

    request.mapping(
            "{\n" +
                    "        \"properties\": {\n" +
                    "            \"content\": {\n" +
                    "                \"type\": \"text\",\n" +
                    "                \"fielddata\": true,\n" +
                    "                \"analyzer\": \"stopped\",\n" +
                    "                \"index_options\": \"positions\"\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }",
            XContentType.JSON);

    CreateIndexResponse createIndexResponse = restClient.indices().create(request, RequestOptions.DEFAULT);
    System.out.println(createIndexResponse.toString());

  }

  public void postDocuments(HashMap<String, String> documents) {
    BulkRequest request = new BulkRequest();
    for (String key : documents.keySet()) {
      request.add(new IndexRequest("API89").id(key)
              .source(documents.get(key)));
    }
  }

}
