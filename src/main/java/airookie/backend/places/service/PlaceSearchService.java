package airookie.backend.places.service;

import airookie.backend.places.dto.PlaceSearchResponse;
import airookie.backend.places.dto.PlaceSearchResultResponse;
import airookie.backend.places.google.GooglePlaceSearchClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 10;

    private final GooglePlaceSearchClient googlePlaceSearchClient;

    public PlaceSearchResponse search(String query, Integer limit) {
        String trimmedQuery = query.trim();
        int resultLimit = normalizeLimit(limit);
        List<PlaceSearchResultResponse> places = new ArrayList<>();
        Set<String> seenPlaceIds = new HashSet<>();
        Set<String> seenNames = new HashSet<>();
        Set<String> seenLabels = new HashSet<>();
        addTextSearchResults(
                googlePlaceSearchClient.textSearch(trimmedQuery),
                resultLimit,
                places,
                seenPlaceIds,
                seenNames,
                seenLabels
        );

        return new PlaceSearchResponse(places);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private void addTextSearchResults(
            JsonNode root,
            int resultLimit,
            List<PlaceSearchResultResponse> places,
            Set<String> seenPlaceIds,
            Set<String> seenNames,
            Set<String> seenLabels
    ) {
        for (JsonNode result : root.path("results")) {
            if (places.size() >= resultLimit) {
                break;
            }

            PlaceSearchResultResponse place = toTextSearchResponse(result);
            addIfNew(place, places, seenPlaceIds, seenNames, seenLabels);
        }
    }

    private PlaceSearchResultResponse toTextSearchResponse(JsonNode result) {
        JsonNode location = result.path("geometry").path("location");
        return new PlaceSearchResultResponse(
                result.path("place_id").asText(),
                result.path("name").asText(),
                result.path("formatted_address").asText(),
                location.path("lat").isNumber() ? location.path("lat").asDouble() : null,
                location.path("lng").isNumber() ? location.path("lng").asDouble() : null
        );
    }

    private void addIfNew(
            PlaceSearchResultResponse place,
            List<PlaceSearchResultResponse> places,
            Set<String> seenPlaceIds,
            Set<String> seenNames,
            Set<String> seenLabels
    ) {
        if (!isDuplicate(place, seenPlaceIds, seenNames, seenLabels)) {
            places.add(place);
        }
    }

    private boolean isDuplicate(
            PlaceSearchResultResponse place,
            Set<String> seenPlaceIds,
            Set<String> seenNames,
            Set<String> seenLabels
    ) {
        String placeId = place.id();
        String name = normalize(place.name());
        String label = normalize(place.name() + " " + place.detail());

        if (!placeId.isBlank() && !seenPlaceIds.add(placeId)) {
            return true;
        }

        if (!name.isBlank() && !seenNames.add(name)) {
            return true;
        }

        return !seenLabels.add(label);
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", "").toLowerCase(Locale.KOREAN);
    }
}
