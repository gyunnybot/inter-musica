package kr.co.inter_musica.presentation.dto.joinrequest;

import kr.co.inter_musica.domain.enums.JoinRequestStatus;

import java.time.Instant;

public class JoinRequestResponse {
    private Long id;
    private Long teamId;
    private Long positionSlotId;
    private Long applicantUserId;
    private JoinRequestStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public JoinRequestResponse() {}

    public JoinRequestResponse(Long id, Long teamId, Long positionSlotId, Long applicantUserId,
                               JoinRequestStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.teamId = teamId;
        this.positionSlotId = positionSlotId;
        this.applicantUserId = applicantUserId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getTeamId() { return teamId; }
    public Long getPositionSlotId() { return positionSlotId; }
    public Long getApplicantUserId() { return applicantUserId; }
    public JoinRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
