package org.project.doodle.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * A bookable interval [startTime, endTime) within a calendar.
 * Carries a Version column for optimistic locking.
 */
@Entity
@Table(name = "time_slot")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private SlotStatus status = SlotStatus.FREE;

    @Version
    private long version;

    protected TimeSlot() {
    }

    public TimeSlot(Calendar calendar, Instant startTime, Instant endTime) {
        this.calendar = calendar;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = SlotStatus.FREE;
    }

    public boolean isFree() {
        return status == SlotStatus.FREE;
    }

    public Long getId() {
        return id;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public long getVersion() {
        return version;
    }
}
