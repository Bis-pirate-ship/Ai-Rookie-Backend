package airookie.backend.routes.google;

import airookie.backend.routes.domain.ParsedRoute;
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

    public List<ParsedRoute> toParsedRoutes(JsonNode root) {
        List<ParsedRoute> parsedRoutes = new ArrayList<>();
        JsonNode routes = root.path("routes");

        for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
            JsonNode route = routes.get(routeIndex);
            JsonNode leg = route.path("legs").path(0);
            JsonNode stepsNode = leg.path("steps");

            List<RouteStep> steps = new ArrayList<>();

            for (int stepIndex = 0; stepIndex < stepsNode.size(); stepIndex++) {
                JsonNode step = stepsNode.get(stepIndex);
                TravelMode mode = resolveMode(step);
                int distanceMeters = step.path("distance").path("value").asInt(0);
                int durationMinutes = toMinutes(step.path("duration").path("value").asInt(0));
                JsonNode transitDetails = step.path("transit_details");
                String instruction = buildInstruction(step, mode);

                steps.add(new RouteStep(
                        stepIndex + 1,
                        mode,
                        transitLineName(transitDetails),
                        transitVehicleType(transitDetails),
                        transitDetails.path("departure_stop").path("name").asText(null),
                        transitDetails.path("arrival_stop").path("name").asText(null),
                        transitDepartureTime(transitDetails),
                        instruction,
                        durationMinutes,
                        distanceMeters
                ));
            }

            parsedRoutes.add(new ParsedRoute(
                    "google-route-" + (routeIndex + 1),
                    route.path("summary").asText("Google Maps 대중교통 경로"),
                    toMinutes(leg.path("duration").path("value").asInt(0)),
                    leg.path("distance").path("value").asInt(0),
                    steps
            ));
        }

        return parsedRoutes;
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

    private String transitLineName(JsonNode transitDetails) {
        if (transitDetails.isMissingNode()) {
            return null;
        }

        String shortName = transitDetails.path("line").path("short_name").asText(null);
        if (shortName != null && !shortName.isBlank()) {
            return shortName;
        }

        String name = transitDetails.path("line").path("name").asText(null);
        return name == null || name.isBlank() ? null : name;
    }

    private String transitVehicleType(JsonNode transitDetails) {
        if (transitDetails.isMissingNode()) {
            return null;
        }

        String vehicleType = transitDetails.path("line").path("vehicle").path("type").asText(null);
        return vehicleType == null || vehicleType.isBlank() ? null : vehicleType;
    }

    private Long transitDepartureTime(JsonNode transitDetails) {
        JsonNode value = transitDetails.path("departure_time").path("value");
        if (!value.isNumber()) {
            return null;
        }

        return value.asLong();
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

    private int toMinutes(int seconds) {
        if (seconds <= 0) {
            return 0;
        }
        return (int) Math.ceil(seconds / 60.0);
    }
}
