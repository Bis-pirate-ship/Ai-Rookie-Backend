package airookie.backend.routes.domain;

public record RouteStep(
        int order,
        TravelMode mode,
        String instruction,
        int durationMinutes,
        int distanceMeters
) {
}
