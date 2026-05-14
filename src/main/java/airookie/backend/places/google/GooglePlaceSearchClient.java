package airookie.backend.places.google;

import airookie.backend.routes.exception.RouteRecommendationException;
import airookie.backend.routes.google.GoogleMapsProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class GooglePlaceSearchClient {

    private final GoogleMapsProperties properties;
    private final ObjectMapper objectMapper;

    public JsonNode textSearch(String query) {
        if (!properties.hasApiKey()) {
            throw new RouteRecommendationException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "GOOGLE_MAPS_API_KEY가 설정되어 있지 않습니다."
            );
        }

        RestClient restClient = RestClient.create();
        URI uri = UriComponentsBuilder.fromHttpUrl(properties.placesUrl())
                .queryParam("query", query)
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
                    "Google Places API 호출에 실패했습니다."
            );
        } catch (Exception exception) {
            throw new RouteRecommendationException(
                    HttpStatus.BAD_GATEWAY,
                    "Google Places API 응답을 처리하지 못했습니다."
            );
        }
    }

    private void validateGoogleStatus(JsonNode root) {
        String status = root.path("status").asText();
        if ("OK".equals(status) || "ZERO_RESULTS".equals(status)) {
            return;
        }

        String errorMessage = root.path("error_message").asText("Google Places API 오류가 발생했습니다.");
        throw new RouteRecommendationException(HttpStatus.BAD_GATEWAY, errorMessage);
    }
}
