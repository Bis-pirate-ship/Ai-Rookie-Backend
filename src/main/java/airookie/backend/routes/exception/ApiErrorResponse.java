package airookie.backend.routes.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        List<String> messages
) {
    public static ApiErrorResponse of(int status, String error, List<String> messages) {
        return new ApiErrorResponse(OffsetDateTime.now(), status, error, messages);
    }
}
