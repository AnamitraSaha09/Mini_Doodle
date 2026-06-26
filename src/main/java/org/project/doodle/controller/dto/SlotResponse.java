package org.project.doodle.controller.dto;

import org.project.doodle.domain.SlotStatus;
import org.project.doodle.domain.TimeSlot;

import java.time.Instant;

public record SlotResponse(
        Long id,
        Instant startTime,
        Instant endTime,
        SlotStatus status) {
    public static SlotResponse from(TimeSlot s) {
        return new SlotResponse(s.getId(), s.getStartTime(), s.getEndTime(), s.getStatus());
    }
}
