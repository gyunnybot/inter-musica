package kr.co.inter_musica.team.dto;

import kr.co.inter_musica.team.domain.enumm.JoinRequestStatus;

public class AcceptResponse {
    private Long joinRequestId;
    private JoinRequestStatus status;
    private Long teamMemberId;

    public AcceptResponse() {}

    public Long getJoinRequestId() { return joinRequestId; }
    public void setJoinRequestId(Long joinRequestId) { this.joinRequestId = joinRequestId; }

    public JoinRequestStatus getStatus() { return status; }
    public void setStatus(JoinRequestStatus status) { this.status = status; }

    public Long getTeamMemberId() { return teamMemberId; }
    public void setTeamMemberId(Long teamMemberId) { this.teamMemberId = teamMemberId; }
}
