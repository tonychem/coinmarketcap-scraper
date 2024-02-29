package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import exception.*;
import model.CryptocurrencyInfo;
import model.QuoteInfo;

import java.util.ArrayList;
import java.util.List;

public class DefaultMarketResponseParser {

    private static final String BASE_FIAT_CURRENCY = "USD";
    private final ObjectMapper objectMapper;

    private final Configuration jsonPathConfiguration;

    private final ParseContext parseContext;

    protected DefaultMarketResponseParser() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        jsonPathConfiguration = Configuration.builder()
                .jsonProvider(new JacksonJsonNodeJsonProvider())
                .mappingProvider(new JacksonMappingProvider())
                .build();

        parseContext = JsonPath
                .using(jsonPathConfiguration);
    }

    public List<CryptocurrencyInfo> parseCurrencyInfoList(String json) throws JsonProcessingException {
        ArrayNode result = parseContext.parse(json).read("$.data.*", ArrayNode.class);

        List<CryptocurrencyInfo> infos = new ArrayList<>();

        for (JsonNode node : result) {
            JsonNode internalArrayNode = node.get(0);
            CryptocurrencyInfo info = objectMapper.readValue(internalArrayNode.toString(), CryptocurrencyInfo.class);
            QuoteInfo quoteInfo = objectMapper.readValue(internalArrayNode.findPath(BASE_FIAT_CURRENCY).toString(), QuoteInfo.class);
            info.setQuoteInfo(quoteInfo);
            infos.add(info);
        }

        return infos;
    }

    public void handleRateLimitViolation(String json) throws ApiRateLimitExceededException, JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        int fineStatusCode = root.at("/status/error_code").asInt();

        if (fineStatusCode == 1008) {
            throw new MinuteApiRateLimitExceededException("Minute rate limit exceeded");
        } else if (fineStatusCode == 1009) {
            throw new DailyApiRateLimitExceededException("Daily rate limit exceeded");

        } else if (fineStatusCode == 1010) {
            throw new MonthlyApiRateLimitExceededException("Monthly rate limit exceeded");

        } else if (fineStatusCode == 1011) {
            throw new IPApiRateLimitExceededException("IP rate limit exceeded");
        }
    }
}
