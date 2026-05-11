package airookie.backend.ai.dto;

import java.util.List;

public record AiPredictRouteRequest(
        AiUserCondition user,
        List<AiRouteFeature> routes
) {
}
