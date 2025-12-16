package kr.co.inter_musica.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "position_slots")
public class PositionSlotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="team_id", nullable = false)
    private Long teamId;

    @Column(nullable = false, length = 30)
    private String instrument; // "VOCAL" ...

    @Column(nullable = false)
    private int capacity;

    @Column(name="required_level_min", nullable = false, length = 30)
    private String requiredLevelMin;

    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PositionSlotJpaEntity() {}

    public PositionSlotJpaEntity(Long teamId, String instrument, int capacity, String requiredLevelMin) {
        this.teamId = teamId;
        this.instrument = instrument;
        this.capacity = capacity;
        this.requiredLevelMin = requiredLevelMin;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getTeamId() { return teamId; }
    public String getInstrument() { return instrument; }
    public int getCapacity() { return capacity; }
    public String getRequiredLevelMin() { return requiredLevelMin; }
    public Instant getCreatedAt() { return createdAt; }
}
