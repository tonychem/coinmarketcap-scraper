package parser.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import utils.entity.ElasticsearchDatasource;

/**
 * Класс-фабрика для создания клиентов ES
 */
public class ElasticsearchClientFactory {
    private final ElasticsearchDatasource dataSource;

    public ElasticsearchClientFactory(ElasticsearchDatasource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Метод, возвращающий незащищенного ES клиента (отсутствует сертификат SSL и шифрование)
     * @return клиент Elasticsearch
     */
    public ElasticsearchClient getUnsecuredClient() {
        RestClient restClient = RestClient
                .builder(new HttpHost(dataSource.host(), dataSource.port(), dataSource.scheme()))
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
