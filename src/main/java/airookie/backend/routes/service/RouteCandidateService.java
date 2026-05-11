package airookie.backend.routes.service;

import airookie.backend.routes.domain.RouteCandidate;
import airookie.backend.routes.domain.RouteStep;
import airookie.backend.routes.dto.RecommendRouteRequest;
import airookie.backend.routes.dto.RouteCandidatesResponse;
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

    public RouteCandidatesResponse getCandidates(RecommendRouteRequest request) {
        JsonNode googleResponse = googleDirectionsClient.fetchTransitRoutes(request);
        List<RouteCandidate> candidates = googleDirectionsMapper.toCandidates(googleResponse);

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

    private RouteSummaryResponse toSummaryResponse(RouteCandidate candidate) {
        return new RouteSummaryResponse(
                candidate.id(),
                candidate.summary(),
                candidate.totalTimeMinutes(),
                candidate.walkDistanceMeters(),
                candidate.transferCount(),
                candidate.hasElevator(),
                candidate.congestionLevel(),
                candidate.steps().stream()
                        .map(this::toStepResponse)
                        .toList()
        );
    }

    private RouteStepResponse toStepResponse(RouteStep step) {
        return new RouteStepResponse(
                step.order(),
                step.mode(),
                step.instruction(),
                step.durationMinutes(),
                step.distanceMeters()
        );
    }
}
