package repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.*;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;

import java.io.IOException;

/**
 * Класс для DDL операций над ES репозиторием.
 */
public class RepositoryManager {
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";
    private final ElasticsearchClient elasticsearchClient;

    public RepositoryManager(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    /**
     * Проверить, существует ли заданный индекс
     * @param indexName имя индекса
     */
    public boolean checkIndexExist(String indexName) throws IOException {
        BooleanResponse response = elasticsearchClient.indices().exists(c -> c.index(indexName));
        return response.value();
    }

    /**
     * Создать индекс
     * @param indexName имя создаваемого индекса
     */
    public void createIndex(String indexName) throws IOException {
        CreateIndexRequest.Builder createIndexRequestBuilder = new CreateIndexRequest.Builder();
        TypeMapping.Builder typeMappingBuilder = new TypeMapping.Builder();

        typeMappingBuilder.properties("id", new Property.Builder()
                .long_(new LongNumberProperty.Builder().build())
                .build());
        typeMappingBuilder.properties("name", new Property.Builder()
                .keyword(new KeywordProperty.Builder().build())
                .build());
        typeMappingBuilder.properties("symbol", new Property.Builder()
                .keyword(new KeywordProperty.Builder().build())
                .build());
        typeMappingBuilder.properties("slug", new Property.Builder()
                .keyword(new KeywordProperty.Builder().build())
                .build());
        typeMappingBuilder.properties("is_active", new Property.Builder()
                .integer(new IntegerNumberProperty.Builder().build())
                .build());
        typeMappingBuilder.properties("is_fiat", new Property.Builder()
                .integer(new IntegerNumberProperty.Builder().build())
                .build());
        typeMappingBuilder.properties("cmc_rank", new Property.Builder()
                .long_(new LongNumberProperty.Builder().build())
                .build());
        typeMappingBuilder.properties("last_updated", new Property.Builder()
                .date(new DateProperty.Builder()
                        .format(DEFAULT_DATETIME_FORMAT)
                        .build())
                .build());
        typeMappingBuilder.properties("quote.price", new Property.Builder()
                .double_(new DoubleNumberProperty.Builder().build()).build());
        typeMappingBuilder.properties("quote.percent_change_1h", new Property.Builder()
                .double_(new DoubleNumberProperty.Builder().build()).build());
        typeMappingBuilder.properties("quote.percent_change_24h", new Property.Builder()
                .double_(new DoubleNumberProperty.Builder().build()).build());
        typeMappingBuilder.properties("quote.percent_change_7d", new Property.Builder()
                .double_(new DoubleNumberProperty.Builder().build()).build());
        typeMappingBuilder.properties("quote.percent_change_30d", new Property.Builder()
                .double_(new DoubleNumberProperty.Builder().build()).build());
        typeMappingBuilder.properties("quote.last_updated", new Property.Builder()
                .date(new DateProperty.Builder()
                        .format(DEFAULT_DATETIME_FORMAT)
                        .build()).build());

        createIndexRequestBuilder.index(indexName)
                .mappings(typeMappingBuilder.build());

        elasticsearchClient.indices()
                .create(createIndexRequestBuilder.build());
    }
}
