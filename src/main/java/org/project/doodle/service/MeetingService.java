package org.project.doodle.service;

import org.project.doodle.controller.dto.CreateMeetingRequest;
import org.project.doodle.domain.Meeting;
import org.project.doodle.domain.SlotStatus;
import org.project.doodle.domain.TimeSlot;
import org.project.doodle.domain.User;
import org.project.doodle.exception.ConflictException;
import org.project.doodle.exception.NotFoundException;
import org.project.doodle.repository.MeetingRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeetingService {

    private final TimeSlotService slotService;
    private final UserService userService;
    private final MeetingRepository meetingRepository;

    public MeetingService(TimeSlotService slotService, UserService userService, MeetingRepository meetingRepository) {
        this.slotService = slotService;
        this.userService = userService;
        this.meetingRepository = meetingRepository;
    }

    @Transactional
    public Meeting book(Long userId, Long slotId, CreateMeetingRequest req) {
        User user = userService.get(userId);

        TimeSlot slot = slotService.getOwnedSlot(userId, slotId);
        if (!slot.isFree()) {
            throw new ConflictException("Slot " + slotId + " is already booked");
        }

        slot.setStatus(SlotStatus.BUSY);

        // adding the user if the user was not part of the participants list
        req.participants().add(user.getEmail());

        Meeting meeting = new Meeting(
                slot,
                req.title(),
                req.description(),
                req.participants());

        try {
            return meetingRepository.saveAndFlush(meeting);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictException(
                    "Slot " + slotId + " was booked concurrently; please retry");
        }
    }

    @Transactional(readOnly = true)
    public Meeting get(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("Meeting " + meetingId + " not found"));
    }

    @Transactional
    public void cancel(Long meetingId) {
        Meeting meeting = get(meetingId);
        meeting.getSlot().setStatus(SlotStatus.FREE);
        meetingRepository.delete(meeting);
    }
}
