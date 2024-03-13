package entrypoint;

import org.apache.catalina.startup.Tomcat;
import repository.ElasticsearchClientFactory;
import repository.RepositoryManager;
import service.CoinmarketcapClientPool;
import service.ParsingProcessor;
import utils.ApplicationConstantHolder;
import utils.PropertyFileReader;
import utils.entity.Credential;
import webapi.repository.CryptocurrencyInfoRepository;
import webapi.service.CryptocurrencyWebService;
import webapi.servlet.AveragePriceServlet;
import webapi.servlet.MaximumDailyPriceChangeServlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoinmarketcapScraperApplication {
    public static void main(String[] args) throws IOException {
        ApplicationConstantHolder applicationConstants = CoinmarketcapScraperApplication.applicationConstantReader();
        ElasticsearchClientFactory elasticsearchClientFactory
                = new ElasticsearchClientFactory(applicationConstants.getElasticsearchDataSource());

        CryptocurrencyInfoRepository repo = new CryptocurrencyInfoRepository(elasticsearchClientFactory.getUnsecuredClient());
        RepositoryManager repoManager = new RepositoryManager(elasticsearchClientFactory.getUnsecuredClient());

        CryptocurrencyWebService webService = new CryptocurrencyWebService(repo, applicationConstants);

        Tomcat tomcat = new Tomcat();
        TomcatRunner tomcatRunner = new TomcatRunner(tomcat, applicationConstants.getTomcatUrl());
        tomcatRunner.registerServlet(new AveragePriceServlet(webService));
        tomcatRunner.registerServlet(new MaximumDailyPriceChangeServlet(webService));
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.execute(tomcatRunner);

        CoinmarketcapClientPool pool = new CoinmarketcapClientPool(applicationConstants.getCredentials());
        ParsingProcessor processor = new ParsingProcessor(pool, repoManager);
        processor.start(applicationConstants.getSymbols());
    }

    public static ApplicationConstantHolder applicationConstantReader() {
        Map<String, Object> properties = PropertyFileReader.load("/propertyfile.yml");
        ApplicationConstantHolder.Builder constantBuilder = ApplicationConstantHolder.builder();

        List<Credential> credentials = ((List<String>) properties.get("credentials")).stream()
                .map(Credential::new)
                .toList();

        if (credentials.isEmpty()) {
            throw new IllegalStateException("Список api-токенов пуст.");
        }

        String[] symbols = ((List<String>) (((Map<String, Object>) (properties.get("task"))).get("symbols"))).stream()
                .toArray(String[]::new);

        if (symbols.length == 0) {
            throw new IllegalStateException("Список тикеров для отслеживания пуст.");
        }

        constantBuilder.credentials(credentials);
        constantBuilder.symbols(symbols);

        Map<String, Object> elasticsearchProperties = (Map<String, Object>) properties.get("elasticsearch");

        if (elasticsearchProperties.get("host") != null) {
            constantBuilder.elasticsearchHost((String) elasticsearchProperties.get("host"));
        }

        if (elasticsearchProperties.get("port") != null) {
            constantBuilder.elasticsearchPort((int) elasticsearchProperties.get("port"));
        }

        if (elasticsearchProperties.get("scheme") != null) {
            constantBuilder.elasticsearchScheme((String) elasticsearchProperties.get("scheme"));
        }

        Map<String, Object> tomcatProperties = (Map<String, Object>) properties.get("tomcat");

        if (tomcatProperties.get("host") != null) {
            constantBuilder.tomcatHost((String) elasticsearchProperties.get("host"));
        }

        if (tomcatProperties.get("port") != null) {
            constantBuilder.tomcatPort((int) elasticsearchProperties.get("port"));
        }

        return constantBuilder.build();
    }
}
