package org.project.doodle.controller.dto;

import java.time.Instant;
import java.util.List;

public record AvailabilityResponse(
        Long userId,
        Instant from,
        Instant to,
        List<SlotResponse> free,
        List<SlotResponse> busy) {
}
