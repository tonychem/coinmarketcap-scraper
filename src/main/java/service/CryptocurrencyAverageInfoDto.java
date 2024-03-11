package service;

import java.math.BigDecimal;

public record CryptocurrencyAverageInfoDto(String symbol, BigDecimal averageValue, int period) {}
