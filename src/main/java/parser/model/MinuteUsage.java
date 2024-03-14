package parser.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class MinuteUsage {
    @JsonProperty("requests_made")
    private Long requestsMade = -1L;

    @JsonProperty("requests_left")
    private Long requestsLeft = -1L;
}
