package airookie.backend.routes.dto;

public record RouteScoreResponse(
        String routeId,
        double comfortScore
) {
}
