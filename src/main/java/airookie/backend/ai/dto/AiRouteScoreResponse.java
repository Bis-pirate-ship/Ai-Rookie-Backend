package airookie.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiRouteScoreResponse(
        @JsonProperty("route_id")
        String routeId,

        @JsonProperty("comfort_score")
        double comfortScore
) {
}
