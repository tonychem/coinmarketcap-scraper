package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.UserUsageStatistics;
import utils.entity.Credential;

import java.net.http.HttpResponse;

import static utils.ApplicationConstantHolder.defaultObjectMapper;

public class UserUsageStatisticGatherer {
    private final GenericCoinmarketcapHttpClient httpClient;

    private final ObjectMapper objectMapper;

    private static final String KEY_INFO_URL = "/v1/key/info";

    public UserUsageStatisticGatherer() {
        objectMapper = defaultObjectMapper();
        httpClient = new GenericCoinmarketcapHttpClient(KEY_INFO_URL);
    }

    public UserUsageStatistics getStatistics(Credential credential) {
        try {
            HttpResponse<String> response = httpClient.executeGetRequest(DynamicParameterQuery.emptyQuery(), credential);
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String statisticsObjectAsString = root.at("/data/usage").toString();
                UserUsageStatistics statistics = objectMapper.readValue(statisticsObjectAsString, UserUsageStatistics.class);
                return statistics;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //TODO: dummy
        throw new RuntimeException();
    }
}
