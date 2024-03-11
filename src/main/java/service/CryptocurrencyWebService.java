package service;

import model.CryptocurrencyInfo;
import repository.Averages;
import repository.CryptocurrencyInfoRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CryptocurrencyWebService {
    private final CryptocurrencyInfoRepository cryptocurrencyInfoRepository;

    private static final String INDEX_FORMAT = "%s_%d_%d_%d";

    public CryptocurrencyWebService(CryptocurrencyInfoRepository cryptocurrencyInfoRepository) {
        this.cryptocurrencyInfoRepository = cryptocurrencyInfoRepository;
    }

    public CryptocurrencyAverageInfoDto getHourAverageForSymbol(String symbol) {
        OffsetDateTime now = Instant.now().atOffset(ZoneOffset.UTC);
        OffsetDateTime oneHourAgo = now.minusHours(1);

        String indexToday = String.format(INDEX_FORMAT, symbol, now.getYear(), now.getMonthValue(), now.getDayOfYear());
        String indexYesterday = String.format(INDEX_FORMAT, symbol, oneHourAgo.getYear(), oneHourAgo.getMonthValue(),
                oneHourAgo.getDayOfYear());

        try {
            if (now.getDayOfMonth() != oneHourAgo.getDayOfMonth()) {
                Averages partOfAverageYesterday = cryptocurrencyInfoRepository.getAveragePriceForPeriod(indexYesterday,
                        symbol, oneHourAgo, now.truncatedTo(ChronoUnit.DAYS).minusSeconds(1));
                Averages partOfAverageToday = cryptocurrencyInfoRepository.getAveragePriceForPeriod(indexToday,
                        symbol, now.truncatedTo(ChronoUnit.DAYS), now);

                int periodToday = partOfAverageToday.period();
                int periodYesterday = partOfAverageYesterday.period();
                long averageToday = partOfAverageToday.average().longValue();
                long averageYesterday = partOfAverageYesterday.average().longValue();

                long newAverage = (periodYesterday / (periodToday + periodYesterday))
                        * (averageYesterday + (periodToday / periodYesterday) * averageToday);
                return new CryptocurrencyAverageInfoDto(symbol, BigDecimal.valueOf(newAverage),
                        periodToday + periodYesterday);
            } else {
                Averages average = cryptocurrencyInfoRepository.getAveragePriceForPeriod(indexToday, symbol, oneHourAgo,
                        now);
                return new CryptocurrencyAverageInfoDto(symbol, average.average(), average.period());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MaximumDailyChangeCryptocurrencyDto getCryptocurrencyWithMaxDailyPriceChange(String[] symbols) {
        OffsetDateTime now = Instant.now().atOffset(ZoneOffset.UTC);
        String[] indexNames = prepareIndexNames(symbols, now);

        try {
            List<CryptocurrencyInfo> symbolsWithMaxDailyChange = new ArrayList<>();

            for (String index : indexNames) {
                symbolsWithMaxDailyChange.add(cryptocurrencyInfoRepository.getCurrencyWithMaxPriceChangeInLastDay(index));
            }

            Comparator<CryptocurrencyInfo> cryptocurrenciesByDailyPriceChangeComparator = (info1, info2) -> {
                BigDecimal info1PriceChange = info1.getQuoteInfo().getDailyPriceChangeInPercent();
                BigDecimal info2PriceChange = info2.getQuoteInfo().getDailyPriceChangeInPercent();
                return info1PriceChange.compareTo(info2PriceChange);
            };

            return symbolsWithMaxDailyChange.stream()
                    .max(cryptocurrenciesByDailyPriceChangeComparator)
                    .map(cryptocurrencyInfo -> new MaximumDailyChangeCryptocurrencyDto(cryptocurrencyInfo.getTicker(),
                            cryptocurrencyInfo.getQuoteInfo().getDailyPriceChangeInPercent()))
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] prepareIndexNames(String[] symbols, OffsetDateTime datetime) {
        String[] indexNames = new String[symbols.length];

        for (int i = 0; i < symbols.length; i++) {
            indexNames[i] = String.format(INDEX_FORMAT, symbols[i], datetime.getYear(),
                    datetime.getMonthValue(), datetime.getDayOfMonth());
        }

        return indexNames;
    }
}
