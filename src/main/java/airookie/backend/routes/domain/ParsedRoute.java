package airookie.backend.routes.domain;

import java.util.List;

public record ParsedRoute(
        String id,
        String summary,
        int totalTimeMinutes,
        int totalDistanceMeters,
        List<RouteStep> steps
) {
}
