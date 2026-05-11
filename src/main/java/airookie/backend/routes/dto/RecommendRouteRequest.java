package airookie.backend.routes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record RecommendRouteRequest(
        @NotBlank(message = "출발지는 필수입니다.")
        String origin,

        @NotBlank(message = "목적지는 필수입니다.")
        String destination,

        @NotBlank(message = "연령대는 필수입니다.")
        String ageGroup,

        @NotNull(message = "거동 불편 여부는 필수입니다.")
        Boolean mobilityImpaired,

        OffsetDateTime departureTime
) {
}
