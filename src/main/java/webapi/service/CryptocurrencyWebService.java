package webapi.service;

import model.CryptocurrencyInfo;
import utils.ApplicationConstantHolder;
import webapi.dto.Averages;
import webapi.dto.CryptocurrencyAverageInfoDto;
import webapi.dto.MaximumDailyChangeCryptocurrencyDto;
import webapi.repository.CryptocurrencyInfoRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static utils.ApplicationConstantHolder.INDICES_NAME_FORMAT;

public class CryptocurrencyWebService {
    private final CryptocurrencyInfoRepository cryptocurrencyInfoRepository;
    private final ApplicationConstantHolder applicationConstantHolder;

    public CryptocurrencyWebService(CryptocurrencyInfoRepository cryptocurrencyInfoRepository,
                                    ApplicationConstantHolder applicationConstantHolder) {
        this.cryptocurrencyInfoRepository = cryptocurrencyInfoRepository;
        this.applicationConstantHolder = applicationConstantHolder;
    }

    public CryptocurrencyAverageInfoDto getHourAverageForSymbol(String symbol) {
        OffsetDateTime now = Instant.now().atOffset(ZoneOffset.UTC);
        OffsetDateTime oneHourAgo = now.minusHours(1);

        String indexToday = String.format(INDICES_NAME_FORMAT, symbol.toLowerCase(), now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String indexYesterday = String.format(INDICES_NAME_FORMAT, symbol.toLowerCase(), oneHourAgo.getYear(), oneHourAgo.getMonthValue(),
                now.getDayOfMonth());

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

    public MaximumDailyChangeCryptocurrencyDto getCryptocurrencyWithMaxDailyPriceChange() {
        OffsetDateTime now = Instant.now().atOffset(ZoneOffset.UTC);
        String[] indexNames = prepareIndexNames(applicationConstantHolder.getSymbols(), now);

        try {
            List<CryptocurrencyInfo> recentInfos = cryptocurrencyInfoRepository.getLatestCryptocurrencyInfos(indexNames);
            Comparator<CryptocurrencyInfo> maxDailyChangeComparator
                    = Comparator.comparing(info -> info.getQuoteInfo().getDailyPriceChangeInPercent().abs());
            CryptocurrencyInfo maxDailyChangeCryptocurrencyInfo = Collections.max(recentInfos, maxDailyChangeComparator);
            return new MaximumDailyChangeCryptocurrencyDto(maxDailyChangeCryptocurrencyInfo.getTicker(),
                    maxDailyChangeCryptocurrencyInfo.getQuoteInfo().getDailyPriceChangeInPercent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] prepareIndexNames(String[] symbols, OffsetDateTime datetime) {
        String[] indexNames = new String[symbols.length];

        for (int i = 0; i < symbols.length; i++) {
            indexNames[i] = String.format(INDICES_NAME_FORMAT, symbols[i].toLowerCase(), datetime.getYear(),
                    datetime.getMonthValue(), datetime.getDayOfMonth());
        }

        return indexNames;
    }
}
