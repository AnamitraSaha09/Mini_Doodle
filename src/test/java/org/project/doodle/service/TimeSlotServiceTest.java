package org.project.doodle.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.doodle.domain.TimeSlot;
import org.project.doodle.domain.User;
import org.project.doodle.exception.BadRequestException;
import org.project.doodle.exception.ConflictException;
import org.project.doodle.repository.TimeSlotRepository;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {

    private static final Instant START = Instant.parse("2026-07-01T09:00:00Z");
    private static final Instant END = Instant.parse("2026-07-01T09:30:00Z");

    @Mock
    private UserService userService;
    @Mock
    private TimeSlotRepository slotRepository;

    private TimeSlotService slotService;

    @BeforeEach
    void setUp() {
        slotService = new TimeSlotService(userService, slotRepository, new SimpleMeterRegistry());
    }

    @Test
    void creatingSlotWithEndBeforeStartIsRejected() {
        assertThatThrownBy(() -> slotService.create(1L, END, START))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void overlappingSlotIsReportedAsConflict() {
        User user = new User("Adam Smith", "adam@example.com");
        when(userService.get(1L)).thenReturn(user);
        when(slotRepository.saveAndFlush(any(TimeSlot.class)))
                .thenThrow(new DataIntegrityViolationException("excl_slot_overlap"));

        assertThatThrownBy(() -> slotService.create(1L, START, END))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void availabilityWithToBeforeFromIsRejected() {
        assertThatThrownBy(() -> slotService.forWindowAvailability(1L, END, START))
                .isInstanceOf(BadRequestException.class);
    }
}
