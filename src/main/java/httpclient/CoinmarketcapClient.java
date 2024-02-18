package httpclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
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
import java.util.List;

public class CoinmarketcapClient implements AutoCloseable {

    private final Credential credential;
    private final String baseUri;

    private final CoinmarketcapClientFactory callbackFactory;

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    private final Configuration jsonPathConfiguration;

    private boolean isClosed = false;

    protected CoinmarketcapClient(Credential credential, String baseUri,
                                  ObjectMapper objectMapper,
                                  Configuration jsonPathConfiguration,
                                  CoinmarketcapClientFactory callbackFactory) {
        this.credential = credential;
        this.baseUri = baseUri;
        this.objectMapper = objectMapper;
        this.callbackFactory = callbackFactory;
        this.jsonPathConfiguration = jsonPathConfiguration;
        httpClient = HttpClient.newBuilder().build();
    }

    //TODO: add 4xx, 5xx response handling
    public List<CryptocurrencyInfo> getCurrencyInfo(DynamicParameterQuery query) {
        try {
            HttpRequest request = createRequest(query);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseCryptocurrencyInfoList(response.body());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

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

    private HttpRequest createRequest(DynamicParameterQuery query) throws MalformedURLException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUri + query.getQuery()))
                .GET()
                .header("Accept", "application/json")
                .header("X-CMC_PRO_API_KEY", credential.apiToken())
                .build();

        return request;
    }


    @Override
    public void close() throws Exception {
        if (isClosed) {
            throw new IllegalStateException("Client was already closed");
        }
        isClosed = true;
        callbackFactory.reviveClient(this);
    }
}
