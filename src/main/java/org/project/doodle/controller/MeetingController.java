package org.project.doodle.controller;

import jakarta.validation.Valid;
import org.project.doodle.controller.dto.CreateMeetingRequest;
import org.project.doodle.controller.dto.MeetingResponse;
import org.project.doodle.domain.Meeting;
import org.project.doodle.service.MeetingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping("/users/{userId}/slots/{slotId}")
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse book(@PathVariable Long userId,
                                @PathVariable Long slotId,
                                @Valid @RequestBody CreateMeetingRequest req) {
        Meeting meeting = meetingService.book(userId, slotId, req);
        return MeetingResponse.from(meeting);
    }

    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingResponse> get(@PathVariable Long meetingId) {
        return ResponseEntity.ok(MeetingResponse.from(meetingService.get(meetingId)));
    }

    @DeleteMapping("/{meetingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long meetingId) {
        meetingService.cancel(meetingId);
    }
}
