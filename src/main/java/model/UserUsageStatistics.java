package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class UserUsageStatistics {
    @JsonProperty("current_minute")
    private MinuteUsage currentMinuteUsage;

    @JsonProperty("current_month")
    private CreditsInfo currentMonthUsage;

    private final LocalDateTime createdAt = LocalDateTime.now();
}
