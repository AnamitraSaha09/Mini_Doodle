package org.project.doodle.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A meeting created by booking a TimeSlot.
 */
@Entity
@Table(name = "meeting")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private TimeSlot slot;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "meeting_participant", joinColumns = @JoinColumn(name = "meeting_id"))
    @Column(name = "email", nullable = false)
    private Set<String> participants = new LinkedHashSet<>();

    protected Meeting() {
    }

    public Meeting(TimeSlot slot, String title, String description, Set<String> participants) {
        this.slot = slot;
        this.title = title;
        this.description = description;
        if (participants != null) {
            this.participants.addAll(participants);
        }
    }

    public Long getId() {
        return id;
    }

    public TimeSlot getSlot() {
        return slot;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getParticipants() {
        return participants;
    }
}
