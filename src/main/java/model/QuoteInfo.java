package model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Последняя информация о стоимости валюты + изменения за периоды
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class QuoteInfo {
    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty(value = "percent_change_1h")
    private BigDecimal hourlyPriceChangeInPercent;

    @JsonProperty(value = "percent_change_24h")
    private BigDecimal dailyPriceChangeInPercent;

    @JsonProperty(value = "percent_change_7d")
    private BigDecimal weeklyPriceChangeInPercent;

    @JsonProperty(value = "percent_change_30d")
    private BigDecimal monthlyPriceChangeInPercent;

    @JsonProperty(value = "last_updated")
    private LocalDateTime lastUpdated;
}
