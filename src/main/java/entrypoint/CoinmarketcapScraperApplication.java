package entrypoint;

import org.apache.catalina.startup.Tomcat;
import repository.ElasticsearchClientFactory;
import repository.RepositoryManager;
import service.CoinmarketcapClientPool;
import service.ParsingProcessor;
import utils.ApplicationConstantHolder;
import utils.PropertyFileReader;
import webapi.repository.CryptocurrencyInfoRepository;
import webapi.service.CryptocurrencyWebService;
import webapi.servlet.AveragePriceServlet;
import webapi.servlet.MaximumDailyPriceChangeServlet;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoinmarketcapScraperApplication {
    public static void main(String[] args) throws IOException {
        ApplicationConstantHolder applicationConstants = PropertyFileReader.readConstants();
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
}
