package parser.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

import static utils.ApplicationConstantHolder.DEFAULT_DATETIME_PATTERN;

/**
 * Информация о криптовалюте, содержащая рыночные метаданные + последнюю информацию о стоимости валюты
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class CryptocurrencyInfo {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String currencyName;

    @JsonProperty("symbol")
    private String ticker;

    @JsonProperty("slug")
    private String alias;

    @JsonProperty("is_active")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Boolean isActive;

    @JsonProperty("is_fiat")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Boolean isFiat;

    @JsonProperty("cmc_rank")
    private Long cmcRank;

    @JsonProperty("last_updated")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DEFAULT_DATETIME_PATTERN)
    private OffsetDateTime lastUpdated;

    @JsonProperty("quote")
    private QuoteInfo quoteInfo;
}
