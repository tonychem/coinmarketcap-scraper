package webapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Запись содержит информацию о криптовалюте, цена которой наиболее сильно изменилась за последние сутки.
 * @param symbol тикер
 * @param dailyPriceChange процент, на который изменилась стоимость криптовалюты за последние сутки
 */
public record MaximumDailyChangeCryptocurrencyDto(
        @JsonProperty("symbol")
        String symbol,

        @JsonProperty("price_change_percent_value")
        BigDecimal dailyPriceChange) {
}
