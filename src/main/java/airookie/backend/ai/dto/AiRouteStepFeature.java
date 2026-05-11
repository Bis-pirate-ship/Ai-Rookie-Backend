package airookie.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiRouteStepFeature(
        int order,
        String mode,

        @JsonProperty("line_name")
        String lineName,

        @JsonProperty("vehicle_type")
        String vehicleType,

        @JsonProperty("departure_stop_name")
        String departureStopName,

        @JsonProperty("arrival_stop_name")
        String arrivalStopName,

        @JsonProperty("departure_time_epoch_seconds")
        Long departureTimeEpochSeconds,

        @JsonProperty("duration_minutes")
        int durationMinutes,

        @JsonProperty("distance_meters")
        int distanceMeters
) {
}
