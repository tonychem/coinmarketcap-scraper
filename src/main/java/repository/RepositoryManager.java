package repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.*;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import model.CryptocurrencyInfo;

import java.io.IOException;

import static utils.ApplicationConstantHolder.DEFAULT_DATETIME_PATTERN;

/**
 * Класс для DDL операций над ES репозиторием.
 */
public class RepositoryManager {
    private final ElasticsearchClient elasticsearchClient;

    public RepositoryManager(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    /**
     * Проверить, существует ли заданный индекс
     *
     * @param indexName имя индекса
     */
    public boolean checkIndexExist(String indexName) throws IOException {
        BooleanResponse response = elasticsearchClient.indices().exists(c -> c.index(indexName));
        return response.value();
    }

    /**
     * Создать индекс
     *
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
                        .format(DEFAULT_DATETIME_PATTERN)
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
                        .format(DEFAULT_DATETIME_PATTERN)
                        .build()).build());

        createIndexRequestBuilder.index(indexName)
                .mappings(typeMappingBuilder.build());

        elasticsearchClient.indices()
                .create(createIndexRequestBuilder.build());
    }

    /**
     * Сохранить информацию о курсе криптовалюты
     *
     * @param indexName индекс, куда сохранять
     * @param info      объект, несущий информацию о криптовалюте
     */
    public void saveCryptocurrencyInfo(String indexName, CryptocurrencyInfo info) throws IOException {
        IndexResponse response = elasticsearchClient.index(request -> request
                .index(indexName)
                .document(info));
    }
}
