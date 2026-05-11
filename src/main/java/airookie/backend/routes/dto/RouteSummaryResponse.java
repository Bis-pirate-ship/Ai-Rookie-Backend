package airookie.backend.routes.dto;

import java.util.List;

public record RouteSummaryResponse(
        String routeId,
        String summary,
        int totalTimeMinutes,
        int walkDistanceMeters,
        int transferCount,
        boolean hasElevator,
        double congestionLevel,
        List<RouteStepResponse> steps
) {
}
