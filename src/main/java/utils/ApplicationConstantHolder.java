package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import utils.entity.Credential;
import utils.entity.ElasticsearchDatasource;
import utils.entity.TomcatUrl;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ApplicationConstantHolder {

    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);
    public static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.UTC;
    public static final String INDICES_NAME_FORMAT = "%s-%d-%d-%d";

    private List<Credential> credentials;

    private String[] symbols;

    private ElasticsearchDatasource elasticsearchDataSource;

    private TomcatUrl tomcatUrl;

    public List<Credential> getCredentials() {
        return credentials;
    }

    public String[] getSymbols() {
        return symbols;
    }

    public ElasticsearchDatasource getElasticsearchDataSource() {
        return elasticsearchDataSource;
    }

    public TomcatUrl getTomcatUrl() {
        return tomcatUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    public static class Builder {
        private final ApplicationConstantHolder holder = new ApplicationConstantHolder();
        private String elasticsearchScheme = "http";
        private String elasticsearchHost = "localhost";

        private int elasticsearchPort = 9200;

        private String tomcatHost = "localhost";
        private int tomcatPort = 8080;

        public Builder credentials(List<Credential> credentials) {
            holder.credentials = credentials;
            return this;
        }

        public Builder symbols(String[] symbols) {
            holder.symbols = symbols;
            return this;
        }

        public Builder elasticsearchScheme(String elasticsearchScheme) {
            this.elasticsearchScheme = elasticsearchScheme;
            return this;
        }

        public Builder elasticsearchHost(String elasticsearchHost) {
            this.elasticsearchHost = elasticsearchHost;
            return this;
        }

        public Builder elasticsearchPort(int elasticsearchPort) {
            this.elasticsearchPort = elasticsearchPort;
            return this;
        }

        public Builder tomcatHost(String tomcatHost) {
            this.tomcatHost = tomcatHost;
            return this;
        }

        public Builder tomcatPort(int tomcatPort) {
            this.tomcatPort = tomcatPort;
            return this;
        }

        public ApplicationConstantHolder build() {
            ElasticsearchDatasource elasticDatasource = new ElasticsearchDatasource(elasticsearchHost,
                    elasticsearchPort, elasticsearchScheme);
            TomcatUrl servletContainerUrl = new TomcatUrl(tomcatHost, tomcatPort);
            holder.elasticsearchDataSource = elasticDatasource;
            holder.tomcatUrl = servletContainerUrl;
            return holder;
        }
    }
}
