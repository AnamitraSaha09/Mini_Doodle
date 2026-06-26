package org.project.doodle.controller.dto;

import org.project.doodle.domain.Meeting;
import org.project.doodle.domain.TimeSlot;

import java.time.Instant;
import java.util.Set;

public record MeetingResponse(
        Long id,
        Long slotId,
        String title,
        String description,
        Set<String> participants,
        Instant startTime,
        Instant endTime) {

    public static MeetingResponse from(Meeting m) {
        TimeSlot s = m.getSlot();
        return new MeetingResponse(
                m.getId(),
                s.getId(),
                m.getTitle(),
                m.getDescription(),
                m.getParticipants(),
                s.getStartTime(),
                s.getEndTime()
        );
    }
}
