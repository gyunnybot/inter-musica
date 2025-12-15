package kr.co.inter_musica.team.dto;

import kr.co.inter_musica.team.domain.enumm.JoinRequestStatus;

public class JoinRequestResponse {
    private Long joinRequestId;
    private Long teamId;
    private Long slotId;
    private Long applicantUserId;
    private JoinRequestStatus status;
    private String createdAt;

    public JoinRequestResponse() {}

    public Long getJoinRequestId() { return joinRequestId; }
    public void setJoinRequestId(Long joinRequestId) { this.joinRequestId = joinRequestId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public Long getApplicantUserId() { return applicantUserId; }
    public void setApplicantUserId(Long applicantUserId) { this.applicantUserId = applicantUserId; }

    public JoinRequestStatus getStatus() { return status; }
    public void setStatus(JoinRequestStatus status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
