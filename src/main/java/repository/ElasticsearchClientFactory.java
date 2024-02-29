package repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

public class ElasticsearchClientFactory {
    private final String elasticsearchApiKey;
    private final ElasticsearchDataSource dataSource;

    public ElasticsearchClientFactory(String elasticsearchApiKey, ElasticsearchDataSource dataSource) {
        this.elasticsearchApiKey = elasticsearchApiKey;
        this.dataSource = dataSource;
    }

    public ElasticsearchClient getClient() {
        RestClient restClient = RestClient
                .builder(new HttpHost(dataSource.host(), dataSource.port(), dataSource.scheme()))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + elasticsearchApiKey)
                })
                .build();

        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper();

        ObjectMapper objectMapper = jsonpMapper.objectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, jsonpMapper);

        return new ElasticsearchClient(transport);
    }
}
