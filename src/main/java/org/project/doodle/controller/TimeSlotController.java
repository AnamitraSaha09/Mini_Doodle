package org.project.doodle.controller;

import jakarta.validation.Valid;
import org.project.doodle.controller.dto.AvailabilityResponse;
import org.project.doodle.controller.dto.CreateSlotRequest;
import org.project.doodle.controller.dto.SlotResponse;
import org.project.doodle.controller.dto.UpdateSlotRequest;
import org.project.doodle.domain.SlotStatus;
import org.project.doodle.domain.TimeSlot;
import org.project.doodle.service.TimeSlotService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/users/{userId}/slots")
public class TimeSlotController {

    private final TimeSlotService slotService;

    public TimeSlotController(TimeSlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlotResponse create(@PathVariable Long userId,
                               @Valid @RequestBody CreateSlotRequest req) {
        TimeSlot slot = slotService.create(userId, req.startTime(), req.endTime());
        return SlotResponse.from(slot);
    }

    @GetMapping
    public Page<SlotResponse> list(@PathVariable Long userId,
                                   @RequestParam(required = false) SlotStatus status,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, Math.min(size, 200), Sort.by("startTime").ascending());
        return slotService.list(userId, status, pageable).map(SlotResponse::from);
    }

    @PatchMapping("/{slotId}")
    public SlotResponse update(@PathVariable Long userId,
                               @PathVariable Long slotId,
                               @Valid @RequestBody UpdateSlotRequest req) {
        return SlotResponse.from(slotService.update(userId, slotId, req));
    }

    @DeleteMapping("/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId, @PathVariable Long slotId) {
        slotService.delete(userId, slotId);
    }

    @GetMapping("/availability")
    public AvailabilityResponse availability(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return slotService.forWindowAvailability(userId, from, to);
    }
}
