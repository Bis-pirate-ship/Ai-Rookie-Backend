package airookie.backend.routes.dto;

import airookie.backend.routes.domain.TravelMode;

public record RouteStepResponse(
        int order,
        TravelMode mode,
        String instruction,
        int durationMinutes,
        int distanceMeters
) {
}
