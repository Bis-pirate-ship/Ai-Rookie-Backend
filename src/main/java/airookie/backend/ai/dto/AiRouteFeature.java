package airookie.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiRouteFeature(
        @JsonProperty("route_id")
        String routeId,

        @JsonProperty("total_time_minutes")
        int totalTimeMinutes,

        @JsonProperty("walk_distance_meters")
        int walkDistanceMeters,

        @JsonProperty("transfer_count")
        int transferCount,

        @JsonProperty("has_elevator")
        boolean hasElevator,

        @JsonProperty("congestion_level")
        double congestionLevel,

        @JsonProperty("time_slot")
        String timeSlot,

        List<AiRouteStepFeature> steps
) {
}
