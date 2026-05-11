package airookie.backend.routes.controller;

import airookie.backend.routes.dto.GoogleRoutesResponse;
import airookie.backend.routes.dto.RecommendRouteRequest;
import airookie.backend.routes.dto.RouteCandidatesResponse;
import airookie.backend.routes.dto.RouteFeaturesRequest;
import airookie.backend.routes.service.RouteCandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteCandidateService routeCandidateService;

    @PostMapping("/google")
    public ResponseEntity<GoogleRoutesResponse> getGoogleRoutes(@Valid @RequestBody RecommendRouteRequest request) {
        return ResponseEntity.ok(routeCandidateService.getGoogleRoutes(request));
    }

    @PostMapping("/features")
    public ResponseEntity<RouteCandidatesResponse> extractFeatures(@Valid @RequestBody RouteFeaturesRequest request) {
        return ResponseEntity.ok(routeCandidateService.extractFeatures(request));
    }

    @PostMapping("/candidates")
    public ResponseEntity<RouteCandidatesResponse> getCandidates(@Valid @RequestBody RecommendRouteRequest request) {
        return ResponseEntity.ok(routeCandidateService.getCandidates(request));
    }
}
