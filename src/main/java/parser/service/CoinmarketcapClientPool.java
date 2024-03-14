package parser.service;

import parser.exception.ClientPoolEmptyException;
import parser.exception.CredentialListAbsentException;
import utils.entity.Credential;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Пул клиентов сервиса. Пул содержит приоритетную очередь из клиентов, упорядоченную по количеству оставшихся месячных
 * запросов по убыванию.
 */
public class CoinmarketcapClientPool {
    private static final String LATEST_QUOTE_UPDATES_URL = "/v2/cryptocurrency/quotes/latest?";

    private final PriorityQueue<CoinmarketcapClient> clientQueue;

    private final List<Credential> credentials;


    public CoinmarketcapClientPool(List<Credential> credentials) throws CredentialListAbsentException {
        if (credentials == null || credentials.isEmpty()) {
            throw new CredentialListAbsentException("Список api-токенов пуст или null.");
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

    public CoinmarketcapClient getClient() throws ClientPoolEmptyException {
        return Optional.ofNullable(clientQueue.poll())
                .orElseThrow(() -> new ClientPoolEmptyException("Отсутствуют клиенты с остатком кредитов за текущий месяц"));
    }

    public void addClient(CoinmarketcapClient client) {
        Objects.requireNonNull(client);
        clientQueue.offer(client);
    }

    public void removeExpiredClients() {
        clientQueue.removeIf(client -> client.getUserUsageStatistics().getCurrentMonthUsage().getCreditsLeft() <= 0);
    }
}
