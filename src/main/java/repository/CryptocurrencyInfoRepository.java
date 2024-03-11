package repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.AverageAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import model.CryptocurrencyInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Класс для обработки DDL команд репозиториев криптовалют.
 */
public class CryptocurrencyInfoRepository {
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
    private final ElasticsearchClient elasticsearchClient;

    public CryptocurrencyInfoRepository(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
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

    /**
     * Получить среднее значение криптовалюты за период.
     *
     * @param indexName имя индекса, в котором происходит поиск
     * @param symbol    тикер криптовалюты
     * @param fromIncl  дата (вкл.), с которой происходит поиск
     * @param toIncl    дата (вкл.), до которой происходит поиск
     * @return информация о средней величине
     */
    public Averages getAveragePriceForPeriod(String indexName, String symbol, OffsetDateTime fromIncl,
                                             OffsetDateTime toIncl)
            throws IOException {
        Query symbolQuery = new Query.Builder()
                .match(new MatchQuery.Builder()
                        .field("symbol")
                        .query(symbol)
                        .build())
                .build();
        Query timeRangeQuery = new Query.Builder()
                .range(new RangeQuery.Builder()
                        .field("last_updated")
                        .gte(JsonData.of(fromIncl.format(DEFAULT_FORMATTER)))
                        .lte(JsonData.of(toIncl.format(DEFAULT_FORMATTER)))
                        .build())
                .build();

        BoolQuery boolQuery = new BoolQuery.Builder()
                .must(symbolQuery)
                .filter(timeRangeQuery)
                .build();

        SearchResponse<Void> aggregation = elasticsearchClient.search(
                c -> c.index(indexName)
                        .query(queryBuilder -> queryBuilder.bool(boolQuery))
                        .aggregations("average-by-quote-price", a ->
                                a.avg(new AverageAggregation.Builder()
                                        .field("quote.price")
                                        .build())),
                Void.class
        );

        return new Averages(BigDecimal.valueOf(aggregation.aggregations().get("average-by-quote-price").avg().value()),
                aggregation.hits().hits().size());
    }

    /**
     * Получить информацию о наибольшем изменении цены криптовалют за последние 24 часа.
     *
     * @param indexName индекс, в котором происходит поиск
     */
    public CryptocurrencyInfo getCurrencyWithMaxPriceChangeInLastDay(String indexName) throws IOException {
        SortOptions sortOptions = SortOptions.of(
                sortBuilder -> sortBuilder.field(
                        fieldSortBuilder -> fieldSortBuilder
                                .field("last_updated")
                                .order(SortOrder.Desc)
                )
        );

        SearchResponse<CryptocurrencyInfo> searchResponse = elasticsearchClient.search(
                searchRequest -> searchRequest
                        .index(indexName)
                        .size(1)
                        .sort(sortOptions), CryptocurrencyInfo.class
        );

        List<Hit<CryptocurrencyInfo>> queryResult = searchResponse.hits().hits();
        return queryResult.isEmpty() ? null : queryResult.get(0).source();
    }
}
