package parser.service;

import parser.model.CryptocurrencyInfo;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Задание по получению информации клиентов по заданному запросу, исполняемое в ThreadPool-e
 */
public class ClientTask implements Callable<List<CryptocurrencyInfo>> {
    private final CoinmarketcapClient client;

    private final DynamicParameterQuery query;

    public ClientTask(CoinmarketcapClient client, DynamicParameterQuery query) {
        this.client = client;
        this.query = query;
    }

    @Override
    public List<CryptocurrencyInfo> call() throws Exception {
        return client.getCurrencyInfo(query);
    }
}
