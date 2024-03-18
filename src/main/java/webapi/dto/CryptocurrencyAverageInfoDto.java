package webapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Запись содержит информацию о среднем значении криптовалюты
 * @param symbol тикер
 * @param averageValue средняя стоимость криптовалюты
 * @param period количество точек, по которым было расчитано среднее (1 точка = 1 минута)
 */
public record CryptocurrencyAverageInfoDto(
        @JsonProperty("symbol")
        String symbol,
        @JsonProperty("average_value_usd")
        BigDecimal averageValue,
        @JsonProperty("points_included")
        int period) {}
