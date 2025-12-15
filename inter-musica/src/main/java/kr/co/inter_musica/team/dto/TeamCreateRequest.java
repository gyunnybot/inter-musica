package kr.co.inter_musica.team.dto;

public class TeamCreateRequest {
    private String name;
    private String intro;
    private String goal;
    private String practiceInfo;

    public TeamCreateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getPracticeInfo() { return practiceInfo; }
    public void setPracticeInfo(String practiceInfo) { this.practiceInfo = practiceInfo; }
}
