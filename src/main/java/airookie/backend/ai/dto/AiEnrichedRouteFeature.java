package airookie.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiEnrichedRouteFeature(
        @JsonProperty("route_id")
        String routeId,

        @JsonProperty("congestion_level")
        double congestionLevel,

        @JsonProperty("has_elevator")
        boolean hasElevator,

        @JsonProperty("time_slot")
        String timeSlot
) {
}
