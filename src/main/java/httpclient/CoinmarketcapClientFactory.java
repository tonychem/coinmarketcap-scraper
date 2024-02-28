package httpclient;

import model.Credential;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Класс-фабрика http-клиентов для доступа к рынку. Содержит информацию об url-адресах рынка, очередь http-клиентов,
 * и набор методов для управления сущностями клиентов.
 */
public class CoinmarketcapClientFactory {
    private static final String LATEST_QUOTE_UPDATES_URL = "/v2/cryptocurrency/quotes/latest?";

    private final Queue<CoinmarketcapClient> clientQueue;

    private final List<Credential> credentials;

    public CoinmarketcapClientFactory(List<Credential> credentials) throws IllegalStateException {
        if (credentials == null || credentials.isEmpty()) {
            throw new IllegalStateException("No user tokens were provided or provided list was empty");
        }

        this.credentials = new ArrayList<>(credentials);

        UserUsageStatisticGatherer userUsageStatisticGatherer = new UserUsageStatisticGatherer();

        clientQueue = this.credentials.stream()
                .map(credential -> new CoinmarketcapClient(credential,
                        LATEST_QUOTE_UPDATES_URL,
                        new DefaultMarketResponseParser()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Возвращает объект клиента из пула клиентов.
     *
     * @return Рыночный клиент
     */
    public CoinmarketcapClient getAvailableClient() {
        if (clientQueue.isEmpty()) {
            throw new ClientPoolEmptyException("Currently no clients are available");
        }
        return clientQueue.poll();
    }
}
