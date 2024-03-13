package service;

import utils.entity.Credential;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

/**
 * Пул клиентов сервиса. Пул содержит приоритетную очередь из клиентов, упорядоченную по количеству оставшихся месячных
 * запросов по убыванию.
 */
public class CoinmarketcapClientPool {
    private static final String LATEST_QUOTE_UPDATES_URL = "/v2/cryptocurrency/quotes/latest?";

    private final PriorityQueue<CoinmarketcapClient> clientQueue;

    private final List<Credential> credentials;


    public CoinmarketcapClientPool(List<Credential> credentials) throws IllegalStateException {
        if (credentials == null || credentials.isEmpty()) {
            throw new IllegalStateException("No user tokens were provided or provided list was empty");
        }

        this.credentials = new ArrayList<>(credentials);

        Comparator<CoinmarketcapClient> prioritizingClientsComparatorByRemainingCredits = Comparator.comparing(
                        (CoinmarketcapClient client) -> client.getUserUsageStatistics().getCurrentMonthUsage().getCreditsLeft())
                .reversed();

        clientQueue = this.credentials.stream()
                .map(credential -> new CoinmarketcapClient(credential,
                        LATEST_QUOTE_UPDATES_URL,
                        new DefaultMarketResponseParser()))
                .filter(client -> client.getUserUsageStatistics().getCurrentMonthUsage().getCreditsLeft() > 0)
                .collect(Collectors.toCollection(
                        () -> new PriorityQueue<>(prioritizingClientsComparatorByRemainingCredits)
                ));
    }

    public int getPoolSize() {
        return clientQueue.size();
    }

    public CoinmarketcapClient getClient() {
        return clientQueue.poll();
    }

    public void addClient(CoinmarketcapClient client) {
        clientQueue.offer(client);
    }

    public void removeExpiredClients() {
        clientQueue.removeIf(client -> client.getUserUsageStatistics().getCurrentMonthUsage().getCreditsLeft() <= 0);
    }
}
