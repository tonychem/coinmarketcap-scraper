import client.Credential;
import repository.CryptocurrencyInfoRepository;
import repository.ElasticsearchClientFactory;
import repository.ElasticsearchDataSource;
import repository.RepositoryManager;
import utils.PropertyFileReader;

import java.util.List;
import java.util.Map;

public class CoinmarketcapScraper {
    public static void main(String[] args) {
        Map<String, Object> properties = PropertyFileReader.load("/propertyfile.yml");

        List<Credential> credentials = ((List<String>) properties.get("credentials")).stream()
                .map(Credential::new)
                .toList();
        String[] symbols = ((List<String>) (((Map<String, Object>) (properties.get("task"))).get("symbols"))).stream()
                .toArray(String[]::new);

        Map<String, Object> elasticsearchProperties = (Map<String, Object>) properties.get("elasticsearch");
        ElasticsearchDataSource elasticsearchDataSource
                = new ElasticsearchDataSource((String) elasticsearchProperties.get("host"),
                (int) elasticsearchProperties.get("port"), (String) elasticsearchProperties.get("scheme"));
        ElasticsearchClientFactory elasticsearchClientFactory
                = new ElasticsearchClientFactory(elasticsearchDataSource);

        CryptocurrencyInfoRepository repo = new CryptocurrencyInfoRepository(elasticsearchClientFactory.getUnsecuredClient());
        RepositoryManager repoManager = new RepositoryManager(elasticsearchClientFactory.getUnsecuredClient());
    }
}
