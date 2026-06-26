package org.project.doodle.repository;

import org.project.doodle.domain.SlotStatus;
import org.project.doodle.domain.TimeSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    Page<TimeSlot> findByCalendarId(Long calendarId, Pageable pageable);

    Page<TimeSlot> findByCalendarIdAndStatus(Long calendarId, SlotStatus status, Pageable pageable);

    @Query("""
            select s from TimeSlot s
            where s.calendar.id = :calendarId
              and s.startTime < :to
              and s.endTime > :from
            order by s.startTime asc
            """)
    List<TimeSlot> findInWindow(@Param("calendarId") Long calendarId,
                                @Param("from") Instant from,
                                @Param("to") Instant to);
}
