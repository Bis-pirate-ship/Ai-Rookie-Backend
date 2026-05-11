package airookie.backend.routes.controller;

import airookie.backend.routes.dto.RecommendRouteRequest;
import airookie.backend.routes.dto.RouteCandidatesResponse;
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

    @PostMapping("/candidates")
    public ResponseEntity<RouteCandidatesResponse> getCandidates(@Valid @RequestBody RecommendRouteRequest request) {
        return ResponseEntity.ok(routeCandidateService.getCandidates(request));
    }
}
