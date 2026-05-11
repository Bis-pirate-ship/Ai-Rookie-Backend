package airookie.backend.routes.domain;

import java.util.List;

public record RouteCandidate(
        String id,
        String summary,
        int totalTimeMinutes,
        int totalDistanceMeters,
        int walkDistanceMeters,
        int transferCount,
        boolean hasElevator,
        double congestionLevel,
        String timeSlot,
        List<RouteStep> steps
) {
}
