package airookie.backend.routes.dto;

import java.util.List;

public record ParsedRouteResponse(
        String routeId,
        String summary,
        int totalTimeMinutes,
        int totalDistanceMeters,
        List<RouteStepResponse> steps
) {
}
