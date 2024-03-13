package webapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CryptocurrencyAverageInfoDto(
        @JsonProperty("symbol")
        String symbol,
        @JsonProperty("average_value")
        BigDecimal averageValue,
        @JsonProperty("points_calculated")
        int period) {}
