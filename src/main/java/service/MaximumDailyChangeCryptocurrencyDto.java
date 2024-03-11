package service;

import java.math.BigDecimal;

public record MaximumDailyChangeCryptocurrencyDto(String symbol, BigDecimal dailyPriceChange) {}
