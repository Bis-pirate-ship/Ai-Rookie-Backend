package airookie.backend.routes.service;

import airookie.backend.routes.domain.ParsedRoute;
import airookie.backend.routes.domain.RouteCandidate;
import airookie.backend.routes.domain.RouteStep;
import airookie.backend.routes.domain.TravelMode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteFeatureExtractor {

    public List<RouteCandidate> extract(List<ParsedRoute> parsedRoutes) {
        return parsedRoutes.stream()
                .map(this::extract)
                .toList();
    }

    private RouteCandidate extract(ParsedRoute parsedRoute) {
        int walkDistanceMeters = calculateWalkDistance(parsedRoute.steps());
        int transferCount = calculateTransferCount(parsedRoute.steps());
        boolean hasElevator = containsElevator(parsedRoute.steps());

        return new RouteCandidate(
                parsedRoute.id(),
                parsedRoute.summary(),
                parsedRoute.totalTimeMinutes(),
                parsedRoute.totalDistanceMeters(),
                walkDistanceMeters,
                transferCount,
                hasElevator,
                0.0,
                "unknown",
                parsedRoute.steps()
        );
    }

    private int calculateWalkDistance(List<RouteStep> steps) {
        return steps.stream()
                .filter(step -> step.mode() == TravelMode.WALKING)
                .mapToInt(RouteStep::distanceMeters)
                .sum();
    }

    private int calculateTransferCount(List<RouteStep> steps) {
        long transitStepCount = steps.stream()
                .filter(step -> isTransitMode(step.mode()))
                .count();

        return Math.max(0, (int) transitStepCount - 1);
    }

    private boolean containsElevator(List<RouteStep> steps) {
        return steps.stream()
                .map(RouteStep::instruction)
                .anyMatch(this::containsElevator);
    }

    private boolean containsElevator(String instruction) {
        String normalized = instruction.toLowerCase();
        return normalized.contains("엘리베이터") || normalized.contains("elevator");
    }

    private boolean isTransitMode(TravelMode mode) {
        return mode == TravelMode.TRANSIT || mode == TravelMode.BUS || mode == TravelMode.SUBWAY;
    }
}
