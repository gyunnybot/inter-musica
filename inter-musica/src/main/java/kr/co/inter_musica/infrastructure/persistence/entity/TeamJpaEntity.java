package kr.co.inter_musica.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "teams")
public class TeamJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="leader_user_id", nullable = false)
    private Long leaderUserId;

    @Column(name="team_name", nullable = false, length = 80)
    private String teamName;

    @Column(name="practice_region", nullable = false, length = 30)
    private String practiceRegion;

    @Column(name="practice_note")
    private String practiceNote;

    @Column(name="core_time_start")
    private LocalTime coreTimeStart;

    @Column(name="core_time_end")
    private LocalTime coreTimeEnd;

    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TeamJpaEntity() {}

    public TeamJpaEntity(Long leaderUserId, String teamName, String practiceRegion, String practiceNote,
                         LocalTime coreTimeStart, LocalTime coreTimeEnd) {
        this.leaderUserId = leaderUserId;
        this.teamName = teamName;
        this.practiceRegion = practiceRegion;
        this.practiceNote = practiceNote;
        this.coreTimeStart = coreTimeStart;
        this.coreTimeEnd = coreTimeEnd;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getLeaderUserId() { return leaderUserId; }
    public String getTeamName() { return teamName; }
    public String getPracticeRegion() { return practiceRegion; }
    public String getPracticeNote() { return practiceNote; }
    public LocalTime getCoreTimeStart() { return coreTimeStart; }
    public LocalTime getCoreTimeEnd() { return coreTimeEnd; }
    public Instant getCreatedAt() { return createdAt; }

    public void setTeamName(String teamName) { this.teamName = teamName; }
    public void setPracticeRegion(String practiceRegion) { this.practiceRegion = practiceRegion; }
    public void setPracticeNote(String practiceNote) { this.practiceNote = practiceNote; }
}
