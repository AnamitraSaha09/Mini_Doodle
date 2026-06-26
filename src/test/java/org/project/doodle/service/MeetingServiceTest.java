package org.project.doodle.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.doodle.controller.dto.CreateMeetingRequest;
import org.project.doodle.domain.Meeting;
import org.project.doodle.domain.SlotStatus;
import org.project.doodle.domain.TimeSlot;
import org.project.doodle.domain.User;
import org.project.doodle.exception.ConflictException;
import org.project.doodle.repository.MeetingRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    private static final Instant START = Instant.parse("2026-07-01T09:00:00Z");
    private static final Instant END = Instant.parse("2026-07-01T09:30:00Z");

    @Mock
    private TimeSlotService slotService;
    @Mock
    private UserService userService;
    @Mock
    private MeetingRepository meetingRepository;

    private MeetingService meetingService;

    @BeforeEach
    void setUp() {
        meetingService = new MeetingService(slotService, userService, meetingRepository, new SimpleMeterRegistry());
    }

    @Test
    void testBookingFreeSlotMarksItBusyAndAddsOrganizer() {
        User user = new User("Adam Smith", "adam@example.com");
        TimeSlot slot = new TimeSlot(user.getCalendar(), START, END);
        when(userService.get(1L)).thenReturn(user);
        when(slotService.getOwnedSlot(1L, 5L)).thenReturn(slot);
        when(meetingRepository.saveAndFlush(any(Meeting.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateMeetingRequest req = new CreateMeetingRequest(
                "1:1 sync", "weekly", new HashSet<>(Set.of("grace@example.com")));

        Meeting meeting = meetingService.book(1L, 5L, req);

        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BUSY);
        assertThat(meeting.getParticipants())
                .contains("adam@example.com", "grace@example.com");
    }

    @Test
    void testBookingAlreadyBusySlotThrowsConflict() {
        User user = new User("Adam Smith", "adam@example.com");
        TimeSlot slot = new TimeSlot(user.getCalendar(), START, END);
        slot.setStatus(SlotStatus.BUSY);
        when(userService.get(1L)).thenReturn(user);
        when(slotService.getOwnedSlot(1L, 5L)).thenReturn(slot);

        CreateMeetingRequest req = new CreateMeetingRequest("1:1 sync", null, new HashSet<>());

        assertThatThrownBy(() -> meetingService.book(1L, 5L, req))
                .isInstanceOf(ConflictException.class);
        verifyNoInteractions(meetingRepository);
    }
}
