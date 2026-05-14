package airookie.backend.ai.client;

import airookie.backend.ai.dto.AiPredictRouteRequest;
import airookie.backend.ai.dto.AiPredictRouteResponse;
import airookie.backend.routes.exception.RouteRecommendationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AiRouteClient {

    private final AiServiceProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public AiPredictRouteResponse predictRoute(AiPredictRouteRequest request) {
        try {
            String requestBody = toJson(request);
            HttpResponse<String> httpResponse = httpClient.send(
                    HttpRequest.newBuilder(predictRouteUri())
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (httpResponse.statusCode() >= 400) {
                throw new RouteRecommendationException(
                        HttpStatus.BAD_GATEWAY,
                        "FastAPI AI 추천 서버 오류: " + httpResponse.statusCode() + " "
                                + httpResponse.body()
                );
            }

            AiPredictRouteResponse response = fromJson(httpResponse.body());
            validateResponse(response);
            return response;
        } catch (IOException exception) {
            throw new RouteRecommendationException(
                    HttpStatus.BAD_GATEWAY,
                    "FastAPI AI 추천 서버 호출에 실패했습니다."
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RouteRecommendationException(
                    HttpStatus.BAD_GATEWAY,
                    "FastAPI AI 추천 서버 호출이 중단되었습니다."
            );
        }
    }

    private URI predictRouteUri() {
        String baseUrl = properties.baseUrl();
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return URI.create(normalizedBaseUrl + "/predict-route");
    }

    private String toJson(AiPredictRouteRequest request) {
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            if ("null".equals(requestBody)) {
                throw new RouteRecommendationException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "AI 추천 요청 본문이 비어 있습니다."
                );
            }
            return requestBody;
        } catch (JsonProcessingException exception) {
            throw new RouteRecommendationException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AI 추천 요청 JSON 생성에 실패했습니다."
            );
        }
    }

    private AiPredictRouteResponse fromJson(String body) {
        try {
            return objectMapper.readValue(body, AiPredictRouteResponse.class);
        } catch (JsonProcessingException exception) {
            throw new RouteRecommendationException(
                    HttpStatus.BAD_GATEWAY,
                    "FastAPI AI 추천 서버 응답 형식이 올바르지 않습니다."
            );
        }
    }

    private void validateResponse(AiPredictRouteResponse response) {
        if (response == null || response.recommendedRouteId() == null || response.scores() == null) {
            throw new RouteRecommendationException(
                    HttpStatus.BAD_GATEWAY,
                    "FastAPI AI 추천 서버 응답 형식이 올바르지 않습니다."
            );
        }

        if (response.scores().isEmpty()) {
            throw new RouteRecommendationException(
                    HttpStatus.BAD_GATEWAY,
                    "FastAPI AI 추천 서버가 경로 점수를 반환하지 않았습니다."
            );
        }
    }
}
