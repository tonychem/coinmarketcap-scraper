package httpclient;

import exception.ApiRateLimitExceededException;
import model.Credential;
import model.CryptocurrencyInfo;
import model.DynamicParameterQuery;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

/**
 * Реализация клиента рынка.
 */

public class CoinmarketcapClient {

    private final Credential credential;
    private final String baseUri;
    private final HttpClient httpClient;
    private final DefaultMarketResponseParser parser;

    protected CoinmarketcapClient(Credential credential, String baseUri, DefaultMarketResponseParser parser) {
        this.credential = credential;
        this.baseUri = baseUri;
        this.parser = parser;
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
                return parser.parseCurrencyInfoList(response.body());
            } else if (statusCode == 429) {
                parser.handleRateLimitViolation(response.body());
            }

            return Collections.emptyList();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
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
