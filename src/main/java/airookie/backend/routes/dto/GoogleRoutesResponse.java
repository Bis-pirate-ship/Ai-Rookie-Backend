package airookie.backend.routes.dto;

import java.util.List;

public record GoogleRoutesResponse(
        List<ParsedRouteResponse> routes
) {
}
