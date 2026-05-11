package airookie.backend.routes.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.maps")
public record GoogleMapsProperties(
        String apiKey,
        String directionsUrl
) {
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
