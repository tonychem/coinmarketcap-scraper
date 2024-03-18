package parser.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.OffsetDateTime;

import static utils.ApplicationConstantHolder.DEFAULT_ZONE_OFFSET;

/**
 * Содержит информацию о лимитах использования для текущего токена.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class UserUsageStatistics {
    @JsonProperty("current_minute")
    private MinuteUsage currentMinuteUsage;

    @JsonProperty("current_month")
    private CreditsInfo currentMonthUsage;

    private OffsetDateTime createdAt = Instant.now()
            .atOffset(DEFAULT_ZONE_OFFSET);
}
