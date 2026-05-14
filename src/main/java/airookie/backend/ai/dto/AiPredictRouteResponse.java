package airookie.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiPredictRouteResponse(
        @JsonProperty("recommended_route_id")
        String recommendedRouteId,

        List<AiRouteScoreResponse> scores,

        String reason,

        @JsonProperty("enriched_routes")
        List<AiEnrichedRouteFeature> enrichedRoutes
) {
}
