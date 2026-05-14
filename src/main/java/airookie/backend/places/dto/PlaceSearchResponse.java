package airookie.backend.places.dto;

import java.util.List;

public record PlaceSearchResponse(
        List<PlaceSearchResultResponse> places
) {
}
