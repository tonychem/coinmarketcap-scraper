package webapi.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.AverageAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import parser.model.CryptocurrencyInfo;
import webapi.dto.Averages;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static utils.ApplicationConstantHolder.DEFAULT_DATETIME_FORMATTER;

/**
 * Класс для обработки DML команд репозиториев криптовалют.
 */
public class CryptocurrencyInfoRepository {
    private final ElasticsearchClient elasticsearchClient;

    public CryptocurrencyInfoRepository(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
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
                        .gte(JsonData.of(fromIncl.format(DEFAULT_DATETIME_FORMATTER)))
                        .lte(JsonData.of(toIncl.format(DEFAULT_DATETIME_FORMATTER)))
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
                (int) aggregation.hits().total().value());
    }

    /**
     * Получить список информаций о криптовалютах, содержащий наиболее последние записи о каждой паре.
     *
     * @param indexNames индексы, по которым происходит поиск
     */
    public List<CryptocurrencyInfo> getLatestCryptocurrencyInfos(String[] indexNames) throws IOException {
        SortOptions sortOptions = SortOptions.of(
                sortBuilder -> sortBuilder.field(
                        fieldSortBuilder -> fieldSortBuilder
                                .field("last_updated")
                                .order(SortOrder.Desc)
                )
        );

        SearchResponse<CryptocurrencyInfo> searchResponse = elasticsearchClient.search(
                searchRequest -> searchRequest
                        .index(Arrays.asList(indexNames))
                        .size(indexNames.length)
                        .sort(sortOptions), CryptocurrencyInfo.class
        );

        List<Hit<CryptocurrencyInfo>> queryResult = searchResponse.hits().hits();
        return queryResult.stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }
}
