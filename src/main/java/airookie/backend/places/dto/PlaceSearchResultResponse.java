package airookie.backend.places.dto;

public record PlaceSearchResultResponse(
        String id,
        String name,
        String detail,
        Double latitude,
        Double longitude
) {
}
