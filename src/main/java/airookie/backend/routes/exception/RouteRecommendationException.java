package airookie.backend.routes.exception;

import org.springframework.http.HttpStatus;

public class RouteRecommendationException extends RuntimeException {

    private final HttpStatus status;

    public RouteRecommendationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
