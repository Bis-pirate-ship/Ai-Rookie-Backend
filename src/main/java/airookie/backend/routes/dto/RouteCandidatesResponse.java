package airookie.backend.routes.dto;

import java.util.List;

public record RouteCandidatesResponse(
        List<RouteSummaryResponse> candidates
) {
}
