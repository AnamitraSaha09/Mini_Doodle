package org.project.doodle.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateSlotRequest(
        @NotNull Instant startTime,
        @NotNull Instant endTime) {

    @AssertTrue(message = "endTime must be after startTime")
    private boolean isValidRange() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}
