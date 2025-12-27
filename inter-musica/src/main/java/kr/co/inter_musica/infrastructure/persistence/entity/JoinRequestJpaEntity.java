package kr.co.inter_musica.infrastructure.persistence.entity;

import jakarta.persistence.*;
import kr.co.inter_musica.domain.enums.JoinRequestStatus;

import java.time.Instant;

@Entity
@Table(name = "join_requests")
public class JoinRequestJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="team_id", nullable = false)
    private Long teamId;

    @Column(name="position_slot_id", nullable = false)
    private Long positionSlotId;

    @Column(name="applicant_user_id", nullable = false)
    private Long applicantUserId;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JoinRequestStatus status;

    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name="updated_at", nullable = false)
    private Instant updatedAt;

    protected JoinRequestJpaEntity() {}

    public JoinRequestJpaEntity(Long teamId, Long positionSlotId, Long applicantUserId, String message) {
        this.teamId = teamId;
        this.positionSlotId = positionSlotId;
        this.applicantUserId = applicantUserId;
        this.message = message;
        this.status = JoinRequestStatus.APPLIED;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getTeamId() { return teamId; }
    public Long getPositionSlotId() { return positionSlotId; }
    public Long getApplicantUserId() { return applicantUserId; }
    public String getMessage() { return message; }
    public JoinRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(JoinRequestStatus status) { this.status = status; }
}
