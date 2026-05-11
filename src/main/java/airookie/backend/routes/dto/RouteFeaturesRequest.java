package airookie.backend.routes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RouteFeaturesRequest(
        @NotEmpty(message = "feature를 추출할 경로 목록은 필수입니다.")
        List<@Valid ParsedRouteResponse> routes
) {
}
