package webapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record MaximumDailyChangeCryptocurrencyDto(
        @JsonProperty("symbol")
        String symbol,

        @JsonProperty("price_change_value")
        BigDecimal dailyPriceChange) {
}
