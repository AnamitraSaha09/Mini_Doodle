package org.project.doodle.service;

import jakarta.validation.Valid;
import org.project.doodle.controller.dto.UpdateSlotRequest;
import org.project.doodle.domain.Calendar;
import org.project.doodle.domain.SlotStatus;
import org.project.doodle.domain.TimeSlot;
import org.project.doodle.domain.User;
import org.project.doodle.exception.BadRequestException;
import org.project.doodle.exception.ConflictException;
import org.project.doodle.exception.NotFoundException;
import org.project.doodle.repository.TimeSlotRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TimeSlotService {

    private final UserService userService;
    private final TimeSlotRepository slotRepository;

    public TimeSlotService(UserService userService, TimeSlotRepository slotRepository) {
        this.userService = userService;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public TimeSlot create(Long userId, Instant start, Instant end) {
        validateRange(start, end);
        Calendar calendar = userService.get(userId).getCalendar();
        TimeSlot slot = new TimeSlot(calendar, start, end);
        return saveGuardingOverlap(slot);
    }

    @Transactional(readOnly = true)
    public Page<TimeSlot> list(Long userId, SlotStatus status, Pageable pageable) {
        Long calendarId = userService.get(userId).getCalendar().getId();
        return status == null
                ? slotRepository.findByCalendarId(calendarId, pageable)
                : slotRepository.findByCalendarIdAndStatus(calendarId, status, pageable);
    }

    @Transactional
    public TimeSlot update(Long userId, Long slotId, @Valid UpdateSlotRequest req) {
        TimeSlot slot = getOwnedSlot(userId, slotId);

        if (slot.getStatus() == SlotStatus.BUSY
                && (req.startTime() != null || req.endTime() != null)) {
            throw new ConflictException(
                    "Slot " + slotId + " is booked; cancel its meeting before moving it");
        }

        Instant newStart = req.startTime() != null ? req.startTime() : slot.getStartTime();
        Instant newEnd = req.endTime() != null ? req.endTime() : slot.getEndTime();
        validateRange(newStart, newEnd);
        slot.setStartTime(newStart);
        slot.setEndTime(newEnd);

        if (req.status() != null) {
            slot.setStatus(req.status());
        }
        return saveGuardingOverlap(slot);
    }

    @Transactional
    public void delete(Long userId, Long slotId) {
        TimeSlot slot = getOwnedSlot(userId, slotId);
        if (slot.getStatus() == SlotStatus.BUSY) {
            throw new ConflictException(
                    "Slot " + slotId + " is booked; cancel its meeting before deleting it");
        }
        slotRepository.delete(slot);
    }

    @Transactional(readOnly = true)
    public TimeSlot getOwnedSlot(Long userId, Long slotId) {
        User user = userService.get(userId);
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new NotFoundException("Slot " + slotId + " not found"));
        if (!slot.getCalendar().getId().equals(user.getCalendar().getId())) {
            throw new NotFoundException("Slot " + slotId + " not found for user " + userId);
        }
        return slot;
    }

    private TimeSlot saveGuardingOverlap(TimeSlot slot) {
        try {
            return slotRepository.saveAndFlush(slot);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "Time slot overlaps an existing slot in this calendar");
        }
    }

    private void validateRange(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new BadRequestException("startTime and endTime are required");
        }
        if (!end.isAfter(start)) {
            throw new BadRequestException("endTime must be after startTime");
        }
    }
}
