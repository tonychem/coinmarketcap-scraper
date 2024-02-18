package model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonProperty("fiat")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Boolean isFiat;

    @JsonProperty("cmc_rank")
    private Long cmcRank;

    @JsonProperty("last_updated")
    private LocalDateTime lastUpdated;

    @JsonProperty("quote")
    private QuoteInfo quoteInfo;
}
