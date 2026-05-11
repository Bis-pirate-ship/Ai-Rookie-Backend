package airookie.backend.routes.service;

import airookie.backend.ai.client.AiRouteClient;
import airookie.backend.ai.dto.AiEnrichedRouteFeature;
import airookie.backend.ai.dto.AiPredictRouteRequest;
import airookie.backend.ai.dto.AiPredictRouteResponse;
import airookie.backend.ai.dto.AiRouteFeature;
import airookie.backend.ai.dto.AiRouteScoreResponse;
import airookie.backend.ai.dto.AiRouteStepFeature;
import airookie.backend.ai.dto.AiUserCondition;
import airookie.backend.routes.domain.RouteCandidate;
import airookie.backend.routes.domain.RouteStep;
import airookie.backend.routes.dto.RecommendRouteRequest;
import airookie.backend.routes.dto.RecommendedRouteResponse;
import airookie.backend.routes.dto.RouteRecommendationResponse;
import airookie.backend.routes.dto.RouteScoreResponse;
import airookie.backend.routes.dto.RouteStepResponse;
import airookie.backend.routes.dto.RouteSummaryResponse;
import airookie.backend.routes.exception.RouteRecommendationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteRecommendationService {

    private final RouteCandidateService routeCandidateService;
    private final AiRouteClient aiRouteClient;

    public RouteRecommendationResponse recommend(RecommendRouteRequest request) {
        List<RouteCandidate> candidates = routeCandidateService.getCandidateFeatures(request);
        AiPredictRouteResponse aiResponse = aiRouteClient.predictRoute(toAiRequest(request, candidates));

        Map<String, RouteCandidate> candidateById = candidates.stream()
                .collect(Collectors.toMap(RouteCandidate::id, Function.identity()));
        RouteCandidate recommended = candidateById.get(aiResponse.recommendedRouteId());

        if (recommended == null) {
            throw new RouteRecommendationException(
                    HttpStatus.BAD_GATEWAY,
                    "AI 추천 결과와 일치하는 경로 후보를 찾지 못했습니다."
            );
        }

        Map<String, Double> scoreByRouteId = aiResponse.scores().stream()
                .collect(Collectors.toMap(AiRouteScoreResponse::routeId, AiRouteScoreResponse::comfortScore));
        List<AiEnrichedRouteFeature> enrichedRoutes = aiResponse.enrichedRoutes() == null
                ? Collections.emptyList()
                : aiResponse.enrichedRoutes();
        Map<String, AiEnrichedRouteFeature> enrichedByRouteId = enrichedRoutes.stream()
                .collect(Collectors.toMap(AiEnrichedRouteFeature::routeId, Function.identity()));

        return new RouteRecommendationResponse(
                toRecommendedRouteResponse(
                        recommended,
                        scoreByRouteId.getOrDefault(recommended.id(), 0.0),
                        aiResponse.reason(),
                        enrichedByRouteId.get(recommended.id())
                ),
                aiResponse.scores().stream()
                        .map(score -> new RouteScoreResponse(score.routeId(), score.comfortScore()))
                        .toList(),
                candidates.stream()
                        .map(candidate -> toSummaryResponse(candidate, enrichedByRouteId.get(candidate.id())))
                        .toList()
        );
    }

    private AiPredictRouteRequest toAiRequest(RecommendRouteRequest request, List<RouteCandidate> candidates) {
        return new AiPredictRouteRequest(
                new AiUserCondition(request.ageGroup(), Boolean.TRUE.equals(request.mobilityImpaired())),
                candidates.stream()
                        .map(this::toAiRouteFeature)
                        .toList()
        );
    }

    private AiRouteFeature toAiRouteFeature(RouteCandidate candidate) {
        return new AiRouteFeature(
                candidate.id(),
                candidate.totalTimeMinutes(),
                candidate.walkDistanceMeters(),
                candidate.transferCount(),
                candidate.hasElevator(),
                candidate.congestionLevel(),
                candidate.timeSlot(),
                candidate.steps().stream()
                        .map(this::toAiRouteStepFeature)
                        .toList()
        );
    }

    private AiRouteStepFeature toAiRouteStepFeature(RouteStep step) {
        return new AiRouteStepFeature(
                step.order(),
                step.mode().name(),
                step.lineName(),
                step.vehicleType(),
                step.departureStopName(),
                step.arrivalStopName(),
                step.departureTimeEpochSeconds(),
                step.durationMinutes(),
                step.distanceMeters()
        );
    }

    private RecommendedRouteResponse toRecommendedRouteResponse(
            RouteCandidate candidate,
            double comfortScore,
            String recommendationReason,
            AiEnrichedRouteFeature enriched
    ) {
        return new RecommendedRouteResponse(
                candidate.id(),
                candidate.summary(),
                candidate.totalTimeMinutes(),
                candidate.walkDistanceMeters(),
                candidate.transferCount(),
                hasElevator(candidate, enriched),
                congestionLevel(candidate, enriched),
                timeSlot(candidate, enriched),
                comfortScore,
                recommendationReason,
                candidate.steps().stream()
                        .map(this::toStepResponse)
                        .toList()
        );
    }

    private RouteSummaryResponse toSummaryResponse(RouteCandidate candidate, AiEnrichedRouteFeature enriched) {
        return new RouteSummaryResponse(
                candidate.id(),
                candidate.summary(),
                candidate.totalTimeMinutes(),
                candidate.walkDistanceMeters(),
                candidate.transferCount(),
                hasElevator(candidate, enriched),
                congestionLevel(candidate, enriched),
                timeSlot(candidate, enriched),
                candidate.steps().stream()
                        .map(this::toStepResponse)
                        .toList()
        );
    }

    private boolean hasElevator(RouteCandidate candidate, AiEnrichedRouteFeature enriched) {
        return enriched == null ? candidate.hasElevator() : enriched.hasElevator();
    }

    private double congestionLevel(RouteCandidate candidate, AiEnrichedRouteFeature enriched) {
        return enriched == null ? candidate.congestionLevel() : enriched.congestionLevel();
    }

    private String timeSlot(RouteCandidate candidate, AiEnrichedRouteFeature enriched) {
        return enriched == null ? candidate.timeSlot() : enriched.timeSlot();
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
