package airookie.backend.routes.google;

import airookie.backend.routes.dto.RecommendRouteRequest;
import airookie.backend.routes.exception.RouteRecommendationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class GoogleDirectionsClient {

    private final GoogleMapsProperties properties;
    private final ObjectMapper objectMapper;

    public JsonNode fetchTransitRoutes(RecommendRouteRequest request) {
        if (!properties.hasApiKey()) {
            throw new RouteRecommendationException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "GOOGLE_MAPS_API_KEY가 설정되어 있지 않습니다."
            );
        }

        RestClient restClient = RestClient.create();
        long departureTime = toGoogleDepartureTime(request.departureTime());
        URI uri = UriComponentsBuilder.fromHttpUrl(properties.directionsUrl())
                .queryParam("origin", request.origin())
                .queryParam("destination", request.destination())
                .queryParam("mode", "transit")
                .queryParam("alternatives", true)
                .queryParam("departure_time", departureTime)
                .queryParam("language", "ko")
                .queryParam("region", "kr")
                .queryParam("key", properties.apiKey())
                .build()
                .encode()
                .toUri();

        try {
            String response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            validateGoogleStatus(root);
            return root;
        } catch (RouteRecommendationException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new RouteRecommendationException(
                    HttpStatus.BAD_GATEWAY,
                    "Google Maps API 호출에 실패했습니다."
            );
        } catch (Exception exception) {
            throw new RouteRecommendationException(
                    HttpStatus.BAD_GATEWAY,
                    "Google Maps API 응답을 처리하지 못했습니다."
            );
        }
    }

    private long toGoogleDepartureTime(OffsetDateTime departureTime) {
        if (departureTime == null) {
            return OffsetDateTime.now().toEpochSecond();
        }
        return departureTime.toEpochSecond();
    }

    private void validateGoogleStatus(JsonNode root) {
        String status = root.path("status").asText();
        if ("OK".equals(status)) {
            return;
        }

        if ("ZERO_RESULTS".equals(status)) {
            throw new RouteRecommendationException(
                    HttpStatus.NOT_FOUND,
                    "조건에 맞는 대중교통 경로를 찾지 못했습니다."
            );
        }

        String errorMessage = root.path("error_message").asText("Google Maps API 오류가 발생했습니다.");
        throw new RouteRecommendationException(HttpStatus.BAD_GATEWAY, errorMessage);
    }
}
