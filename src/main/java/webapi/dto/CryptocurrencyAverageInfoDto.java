package webapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CryptocurrencyAverageInfoDto(
        @JsonProperty("symbol")
        String symbol,
        @JsonProperty("average_value_usd")
        BigDecimal averageValue,
        @JsonProperty("points_included")
        int period) {}
