package kr.co.inter_musica.team.dto;

import kr.co.inter_musica.team.domain.enumm.JoinRequestStatus;

public class MyJoinRequestResponse {
    private Long joinRequestId;
    private Long teamId;
    private String teamName;
    private Long slotId;
    private String instrument;
    private JoinRequestStatus status;
    private String createdAt;

    public MyJoinRequestResponse() {}

    public Long getJoinRequestId() { return joinRequestId; }
    public void setJoinRequestId(Long joinRequestId) { this.joinRequestId = joinRequestId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public JoinRequestStatus getStatus() { return status; }
    public void setStatus(JoinRequestStatus status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
