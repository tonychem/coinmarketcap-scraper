package httpclient;

import model.Credential;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GenericCoinmarketcapHttpClient {

    private static final String MARKET_HOST_URL = "https://pro-api.coinmarketcap.com";
    private final String resourceURL;

    private final HttpClient httpClient;

    public GenericCoinmarketcapHttpClient(String resourceURL) {
        this.resourceURL = MARKET_HOST_URL + resourceURL;
        httpClient = HttpClient.newHttpClient();
    }

    public HttpResponse<String> executeGetRequest(DynamicParameterQuery query, Credential credential) {
        try {
            HttpRequest request = createRequest(query, credential);
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Вспомогательный метод для создания объекта HttpRequest-a на основе кастомного динамического запроса
     *
     * @param query динамический запрос
     * @return
     */
    private HttpRequest createRequest(DynamicParameterQuery query, Credential credential) {
        return HttpRequest.newBuilder()
                .uri(URI.create(resourceURL + query.getQuery()))
                .GET()
                .header("Accept", "application/json")
                .header("X-CMC_PRO_API_KEY", credential.apiToken())
                .build();
    }
}
