package kr.co.inter_musica.team.dto;

public class TeamResponse {
    private Long teamId;
    private Long leaderUserId;
    private String name;
    private String intro;
    private String goal;
    private String practiceInfo;

    public TeamResponse() {}

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getLeaderUserId() { return leaderUserId; }
    public void setLeaderUserId(Long leaderUserId) { this.leaderUserId = leaderUserId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getPracticeInfo() { return practiceInfo; }
    public void setPracticeInfo(String practiceInfo) { this.practiceInfo = practiceInfo; }
}
