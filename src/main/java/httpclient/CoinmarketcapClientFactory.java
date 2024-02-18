package httpclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class CoinmarketcapClientFactory {
    private final Queue<CoinmarketcapClient> clientQueue;
    private static final String DEFAULT_API_HOST = "https://pro-api.coinmarketcap.com";
    private static final String DEFAULT_LATEST_QUOTE_UPDATES_URL = "/v2/cryptocurrency/quotes/latest?";
    private final List<Credential> credentials;

    public CoinmarketcapClientFactory(List<Credential> credentials) throws IllegalStateException {
        if (credentials == null || credentials.isEmpty()) {
            throw new IllegalStateException("No user tokens were provided or provided list was empty");
        }

        this.credentials = new ArrayList<>(credentials);

        clientQueue = this.credentials.stream()
                .map(credential -> new CoinmarketcapClient(credential,
                        DEFAULT_API_HOST + DEFAULT_LATEST_QUOTE_UPDATES_URL,
                        this.generateObjectMapper(), this.generateJsonPathConfiguration(), this))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public CoinmarketcapClient getAvailableClient() {
        if (clientQueue.isEmpty()) {
            throw new ClientPoolEmptyException("Currently no clients are available");
        }
        return clientQueue.poll();
    }

    protected void reviveClient(CoinmarketcapClient client) {
        clientQueue.offer(client);
    }

    protected ObjectMapper generateObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

    protected Configuration generateJsonPathConfiguration() {
        return Configuration.builder()
                .jsonProvider(new JacksonJsonNodeJsonProvider())
                .mappingProvider(new JacksonMappingProvider())
                .build();
    }
}
