package airookie.backend.routes.domain;

public record RouteStep(
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
