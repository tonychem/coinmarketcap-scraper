package repository;

import java.time.LocalDateTime;

public record RepositoryRequest(Iterable<Long> currencyIds, Iterable<String> currencySlugs,
                                Iterable<String> currencyNames,
                                LocalDateTime from, LocalDateTime to) {
}
