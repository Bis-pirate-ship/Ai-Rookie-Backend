package airookie.backend.routes.dto;

import java.util.List;

public record RouteRecommendationResponse(
        RecommendedRouteResponse recommendedRoute,
        List<RouteScoreResponse> scores,
        List<RouteSummaryResponse> candidates
) {
}
