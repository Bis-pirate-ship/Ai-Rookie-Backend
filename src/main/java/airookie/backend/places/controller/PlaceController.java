package airookie.backend.places.controller;

import airookie.backend.places.dto.PlaceSearchResponse;
import airookie.backend.places.service.PlaceSearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceSearchService placeSearchService;

    @GetMapping("/search")
    public ResponseEntity<PlaceSearchResponse> search(
            @RequestParam @NotBlank(message = "검색어는 필수입니다.") String query,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(placeSearchService.search(query, limit));
    }
}
