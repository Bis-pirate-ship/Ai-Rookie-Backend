package airookie.backend.routes.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class RouteExceptionHandler {

    @ExceptionHandler(RouteRecommendationException.class)
    public ResponseEntity<ApiErrorResponse> handleRouteRecommendationException(RouteRecommendationException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(ApiErrorResponse.of(
                        exception.getStatus().value(),
                        exception.getStatus().getReasonPhrase(),
                        List.of(exception.getMessage())
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        List<String> messages = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity
                .badRequest()
                .body(ApiErrorResponse.of(400, "Bad Request", messages));
    }
}
