package kr.co.inter_musica.team.dto;

import kr.co.inter_musica.team.domain.enumm.JoinRequestStatus;

public class JoinRequestStatusResponse {
    private Long joinRequestId;
    private JoinRequestStatus status;

    public JoinRequestStatusResponse() {}

    public Long getJoinRequestId() { return joinRequestId; }
    public void setJoinRequestId(Long joinRequestId) { this.joinRequestId = joinRequestId; }

    public JoinRequestStatus getStatus() { return status; }
    public void setStatus(JoinRequestStatus status) { this.status = status; }
}
