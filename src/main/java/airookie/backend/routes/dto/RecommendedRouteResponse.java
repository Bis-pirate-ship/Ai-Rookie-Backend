package airookie.backend.routes.dto;

import java.util.List;

public record RecommendedRouteResponse(
        String routeId,
        String summary,
        int totalTimeMinutes,
        int walkDistanceMeters,
        int transferCount,
        boolean hasElevator,
        double congestionLevel,
        String timeSlot,
        double comfortScore,
        String recommendationReason,
        List<RouteStepResponse> steps
) {
}
