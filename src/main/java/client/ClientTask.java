package client;

import model.CryptocurrencyInfo;

import java.util.List;
import java.util.concurrent.Callable;

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
