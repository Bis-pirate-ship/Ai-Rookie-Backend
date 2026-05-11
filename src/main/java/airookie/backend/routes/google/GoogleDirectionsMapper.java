package airookie.backend.routes.google;

import airookie.backend.routes.domain.RouteCandidate;
import airookie.backend.routes.domain.RouteStep;
import airookie.backend.routes.domain.TravelMode;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class GoogleDirectionsMapper {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    public List<RouteCandidate> toCandidates(JsonNode root) {
        List<RouteCandidate> candidates = new ArrayList<>();
        JsonNode routes = root.path("routes");

        for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
            JsonNode route = routes.get(routeIndex);
            JsonNode leg = route.path("legs").path(0);
            JsonNode stepsNode = leg.path("steps");

            List<RouteStep> steps = new ArrayList<>();
            int walkDistanceMeters = 0;
            int transitStepCount = 0;
            boolean hasElevator = false;

            for (int stepIndex = 0; stepIndex < stepsNode.size(); stepIndex++) {
                JsonNode step = stepsNode.get(stepIndex);
                TravelMode mode = resolveMode(step);
                int distanceMeters = step.path("distance").path("value").asInt(0);
                int durationMinutes = toMinutes(step.path("duration").path("value").asInt(0));
                String instruction = buildInstruction(step, mode);

                if (mode == TravelMode.WALKING) {
                    walkDistanceMeters += distanceMeters;
                }
                if (isTransitMode(mode)) {
                    transitStepCount++;
                }
                if (containsElevator(instruction)) {
                    hasElevator = true;
                }

                steps.add(new RouteStep(
                        stepIndex + 1,
                        mode,
                        instruction,
                        durationMinutes,
                        distanceMeters
                ));
            }

            candidates.add(new RouteCandidate(
                    "google-route-" + (routeIndex + 1),
                    route.path("summary").asText("Google Maps 대중교통 경로"),
                    toMinutes(leg.path("duration").path("value").asInt(0)),
                    leg.path("distance").path("value").asInt(0),
                    walkDistanceMeters,
                    Math.max(0, transitStepCount - 1),
                    hasElevator,
                    0.0,
                    "unknown",
                    steps
            ));
        }

        return candidates;
    }

    private TravelMode resolveMode(JsonNode step) {
        String travelMode = step.path("travel_mode").asText("");
        if ("WALKING".equals(travelMode)) {
            return TravelMode.WALKING;
        }
        if (!"TRANSIT".equals(travelMode)) {
            return TravelMode.UNKNOWN;
        }

        String vehicleType = step.path("transit_details")
                .path("line")
                .path("vehicle")
                .path("type")
                .asText("");

        return switch (vehicleType) {
            case "BUS" -> TravelMode.BUS;
            case "SUBWAY", "HEAVY_RAIL", "COMMUTER_TRAIN", "RAIL" -> TravelMode.SUBWAY;
            default -> TravelMode.TRANSIT;
        };
    }

    private boolean isTransitMode(TravelMode mode) {
        return mode == TravelMode.TRANSIT || mode == TravelMode.BUS || mode == TravelMode.SUBWAY;
    }

    private String buildInstruction(JsonNode step, TravelMode mode) {
        if (isTransitMode(mode)) {
            JsonNode transitDetails = step.path("transit_details");
            String lineName = transitDetails.path("line").path("short_name").asText(
                    transitDetails.path("line").path("name").asText("대중교통")
            );
            String departureStop = transitDetails.path("departure_stop").path("name").asText("출발 정류장");
            String arrivalStop = transitDetails.path("arrival_stop").path("name").asText("도착 정류장");
            return departureStop + "에서 " + lineName + " 탑승 후 " + arrivalStop + "에서 하차";
        }

        String rawInstruction = step.path("html_instructions").asText("다음 지점으로 이동");
        return stripHtml(rawInstruction);
    }

    private String stripHtml(String value) {
        return HTML_TAG_PATTERN.matcher(value)
                .replaceAll("")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .trim();
    }

    private boolean containsElevator(String instruction) {
        String normalized = instruction.toLowerCase();
        return normalized.contains("엘리베이터") || normalized.contains("elevator");
    }

    private int toMinutes(int seconds) {
        if (seconds <= 0) {
            return 0;
        }
        return (int) Math.ceil(seconds / 60.0);
    }
}
