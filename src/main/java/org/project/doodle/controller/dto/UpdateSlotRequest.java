package org.project.doodle.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import org.project.doodle.domain.SlotStatus;

import java.time.Instant;

public record UpdateSlotRequest(
        Instant startTime,
        Instant endTime,
        SlotStatus status) {

    @AssertTrue(message = "endTime must be after startTime")
    private boolean isValidRange() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}
