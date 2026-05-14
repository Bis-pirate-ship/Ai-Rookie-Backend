package airookie.backend.routes.service;

import airookie.backend.routes.domain.ParsedRoute;
import airookie.backend.routes.domain.RouteCandidate;
import airookie.backend.routes.domain.RouteStep;
import airookie.backend.routes.dto.GoogleRoutesResponse;
import airookie.backend.routes.dto.ParsedRouteResponse;
import airookie.backend.routes.dto.RecommendRouteRequest;
import airookie.backend.routes.dto.RouteCandidatesResponse;
import airookie.backend.routes.dto.RouteFeaturesRequest;
import airookie.backend.routes.dto.RouteStepResponse;
import airookie.backend.routes.dto.RouteSummaryResponse;
import airookie.backend.routes.exception.RouteRecommendationException;
import airookie.backend.routes.google.GoogleDirectionsClient;
import airookie.backend.routes.google.GoogleDirectionsMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteCandidateService {

    private final GoogleDirectionsClient googleDirectionsClient;
    private final GoogleDirectionsMapper googleDirectionsMapper;
    private final RouteFeatureExtractor routeFeatureExtractor;

    public GoogleRoutesResponse getGoogleRoutes(RecommendRouteRequest request) {
        JsonNode googleResponse = googleDirectionsClient.fetchTransitRoutes(request);
        List<ParsedRoute> parsedRoutes = googleDirectionsMapper.toParsedRoutes(googleResponse);

        if (parsedRoutes.isEmpty()) {
            throw new RouteRecommendationException(
                    HttpStatus.NOT_FOUND,
                    "feature를 추출할 대중교통 경로가 없습니다."
            );
        }

        return new GoogleRoutesResponse(
                parsedRoutes.stream()
                        .map(this::toParsedRouteResponse)
                        .toList()
        );
    }

    public RouteCandidatesResponse extractFeatures(RouteFeaturesRequest request) {
        List<ParsedRoute> parsedRoutes = request.routes().stream()
                .map(this::toParsedRoute)
                .toList();
        return toCandidatesResponse(routeFeatureExtractor.extract(parsedRoutes));
    }

    public RouteCandidatesResponse getCandidates(RecommendRouteRequest request) {
        return toCandidatesResponse(getCandidateFeatures(request));
    }

    public List<RouteCandidate> getCandidateFeatures(RecommendRouteRequest request) {
        JsonNode googleResponse = googleDirectionsClient.fetchTransitRoutes(request);
        List<ParsedRoute> parsedRoutes = googleDirectionsMapper.toParsedRoutes(googleResponse);
        return routeFeatureExtractor.extract(parsedRoutes);
    }

    private RouteCandidatesResponse toCandidatesResponse(List<RouteCandidate> candidates) {
        if (candidates.isEmpty()) {
            throw new RouteRecommendationException(
                    HttpStatus.NOT_FOUND,
                    "추천에 사용할 대중교통 경로 후보가 없습니다."
            );
        }

        List<RouteSummaryResponse> candidateResponses = candidates.stream()
                .map(this::toSummaryResponse)
                .toList();

        return new RouteCandidatesResponse(candidateResponses);
    }

    private ParsedRouteResponse toParsedRouteResponse(ParsedRoute parsedRoute) {
        return new ParsedRouteResponse(
                parsedRoute.id(),
                parsedRoute.summary(),
                parsedRoute.totalTimeMinutes(),
                parsedRoute.totalDistanceMeters(),
                parsedRoute.steps().stream()
                        .map(this::toStepResponse)
                        .toList()
        );
    }

    private ParsedRoute toParsedRoute(ParsedRouteResponse response) {
        return new ParsedRoute(
                response.routeId(),
                response.summary(),
                response.totalTimeMinutes(),
                response.totalDistanceMeters(),
                response.steps().stream()
                        .map(this::toRouteStep)
                        .toList()
        );
    }

    private RouteStep toRouteStep(RouteStepResponse step) {
        return new RouteStep(
                step.order(),
                step.mode(),
                step.lineName(),
                step.vehicleType(),
                step.departureStopName(),
                step.arrivalStopName(),
                step.departureTimeEpochSeconds(),
                step.instruction(),
                step.durationMinutes(),
                step.distanceMeters()
        );
    }

    private RouteSummaryResponse toSummaryResponse(RouteCandidate candidate) {
        return new RouteSummaryResponse(
                candidate.id(),
                candidate.summary(),
                candidate.totalTimeMinutes(),
                candidate.walkDistanceMeters(),
                candidate.transferCount(),
                candidate.hasElevator(),
                candidate.congestionLevel(),
                candidate.timeSlot(),
                candidate.steps().stream()
                        .map(this::toStepResponse)
                        .toList()
        );
    }

    private RouteStepResponse toStepResponse(RouteStep step) {
        return new RouteStepResponse(
                step.order(),
                step.mode(),
                step.lineName(),
                step.vehicleType(),
                step.departureStopName(),
                step.arrivalStopName(),
                step.departureTimeEpochSeconds(),
                step.instruction(),
                step.durationMinutes(),
                step.distanceMeters()
        );
    }
}
