package airookie.backend.routes.dto;

import airookie.backend.routes.domain.TravelMode;

public record RouteStepResponse(
        int order,
        TravelMode mode,
        String lineName,
        String vehicleType,
        String departureStopName,
        String arrivalStopName,
        Long departureTimeEpochSeconds,
        String instruction,
        int durationMinutes,
        int distanceMeters
) {
}
