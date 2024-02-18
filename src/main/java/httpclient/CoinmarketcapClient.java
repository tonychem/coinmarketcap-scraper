package httpclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import httpclient.exception.*;
import model.CryptocurrencyInfo;
import model.QuoteInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Реализация клиента рынка.
 */

public class CoinmarketcapClient {

    private final Credential credential;
    private final String baseUri;
    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    private final Configuration jsonPathConfiguration;


    protected CoinmarketcapClient(Credential credential, String baseUri,
                                  ObjectMapper objectMapper,
                                  Configuration jsonPathConfiguration) {
        this.credential = credential;
        this.baseUri = baseUri;
        this.objectMapper = objectMapper;
        this.jsonPathConfiguration = jsonPathConfiguration;
        httpClient = HttpClient.newBuilder().build();
    }

    //TODO: add 4xx, 5xx response handling

    /**
     * Метод возвращает список информации о криптовалюте по заданному запросу.
     *
     * @param query динамический запрос
     * @return Список информации о криптовалютах, согласно переданному динамическому запросу.
     */
    public List<CryptocurrencyInfo> getCurrencyInfo(DynamicParameterQuery query) throws ApiRateLimitExceededException {
        try {
            HttpRequest request = createRequest(query);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();

            if (statusCode == 200) {
                return parseCryptocurrencyInfoList(response.body());
            } else if (statusCode == 429) {
                JsonNode root = objectMapper.readTree(response.body());
                int fineStatusCode = root.at("/status/error_code").asInt();

                if (fineStatusCode == 1008) {
                    throw new MinuteApiRateLimitExceededException("Minute rate limit exceeded");
                } else if (fineStatusCode == 1009) {
                    throw new DailyApiRateLimitExceededException("Daily rate limit exceeded");

                } else if (fineStatusCode == 1010) {
                    throw new MonthlyApiRateLimitExceededException("Monthly rate limit exceeded");

                } else if (fineStatusCode == 1011) {
                    throw new IPApiRateLimitExceededException("IP rate limit exceeded");

                }
            }

            return Collections.emptyList();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Вспомогательный метод для парсинга json строки.
     *
     * @param json в формате строки
     * @return Список информации о криптовалютах
     */
    private List<CryptocurrencyInfo> parseCryptocurrencyInfoList(String json) throws JsonProcessingException {
        ArrayNode result = JsonPath
                .using(jsonPathConfiguration)
                .parse(json).read("$.data.*", ArrayNode.class);

        List<CryptocurrencyInfo> infos = new ArrayList<>();

        for (JsonNode node : result) {
            CryptocurrencyInfo info = objectMapper.readValue(node.toString(), CryptocurrencyInfo.class);
            QuoteInfo quoteInfo = objectMapper.readValue(node.findPath("USD").toString(), QuoteInfo.class);
            info.setQuoteInfo(quoteInfo);
            infos.add(info);
        }

        return infos;
    }

    /**
     * Вспомогательный метод для создания объекта HttpRequest-a на основе кастомного динамического запроса
     *
     * @param query динамический запрос
     * @return
     */
    private HttpRequest createRequest(DynamicParameterQuery query) throws MalformedURLException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUri + query.getQuery()))
                .GET()
                .header("Accept", "application/json")
                .header("X-CMC_PRO_API_KEY", credential.apiToken())
                .build();

        return request;
    }
}
