package entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.startup.Tomcat;
import parser.repository.ElasticsearchClientFactory;
import parser.repository.RepositoryManager;
import parser.service.CoinmarketcapClientPool;
import parser.service.ParsingProcessor;
import utils.PropertyFileReader;
import utils.PropertyHolder;
import webapi.repository.CryptocurrencyInfoRepository;
import webapi.service.CryptocurrencyWebService;
import webapi.servlet.AveragePriceServlet;
import webapi.servlet.MaximumDailyPriceChangeServlet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Точка входа в приложение
 */
@Slf4j
public class CoinmarketcapScraperApplication {
    public static void main(String[] args) {
        PropertyHolder applicationConstants = PropertyFileReader.readConstants();
        ElasticsearchClientFactory elasticsearchClientFactory
                = new ElasticsearchClientFactory(applicationConstants.getElasticsearchDataSource());

        CryptocurrencyInfoRepository repo = new CryptocurrencyInfoRepository(elasticsearchClientFactory.getUnsecuredClient());
        RepositoryManager repoManager = new RepositoryManager(elasticsearchClientFactory.getUnsecuredClient());
        CryptocurrencyWebService webService = new CryptocurrencyWebService(repo, applicationConstants);

        ExecutorService service = Executors.newSingleThreadExecutor();

        try {
            Tomcat tomcat = new Tomcat();
            TomcatRunner tomcatRunner = new TomcatRunner(tomcat, applicationConstants.getTomcatUrl());
            tomcatRunner.registerServlet(new AveragePriceServlet(webService));
            tomcatRunner.registerServlet(new MaximumDailyPriceChangeServlet(webService));
            service.execute(tomcatRunner);

            CoinmarketcapClientPool pool = new CoinmarketcapClientPool(applicationConstants.getCredentials());
            ParsingProcessor processor = new ParsingProcessor(pool, repoManager);
            processor.start(applicationConstants.getSymbols());
        } catch (RuntimeException runtimeException) {
            log.warn("Runtime exception: {}", runtimeException.toString());
        } catch (Exception exception) {
            log.error("Exception: {}", exception.toString());
        }
    }
}
