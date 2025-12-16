package kr.co.inter_musica.presentation.dto.team;

public class CreateTeamResponse {
    private Long teamId;

    public CreateTeamResponse() {}
    public CreateTeamResponse(Long teamId) { this.teamId = teamId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
}
