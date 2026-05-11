package airookie.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiUserCondition(
        @JsonProperty("age_group")
        String ageGroup,

        @JsonProperty("mobility_impaired")
        boolean mobilityImpaired
) {
}
