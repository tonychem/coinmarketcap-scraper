import client.CoinmarketcapClientPool;
import client.Credential;
import service.ParsingProcessor;
import utils.PropertyFileReader;

import java.util.List;
import java.util.Map;

public class CoinmarketcapScraper {
    public static void main(String[] args) throws InterruptedException {
        Map<String, Object> properties = PropertyFileReader.load("/propertyfile.yml");
        List<Credential> credentials = ((List<String>) properties.get("credentials")).stream()
                .map(Credential::new)
                .toList();
        String[] symbols = ((List<String>) (((Map<String, Object>) (properties.get("task"))).get("symbols"))).stream()
                .toArray(String[]::new);

        CoinmarketcapClientPool clientPool = new CoinmarketcapClientPool(credentials);
        ParsingProcessor processor = new ParsingProcessor(clientPool);
    }
}
