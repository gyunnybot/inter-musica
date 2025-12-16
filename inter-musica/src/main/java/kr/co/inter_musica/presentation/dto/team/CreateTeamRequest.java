package kr.co.inter_musica.presentation.dto.team;

import jakarta.validation.constraints.NotBlank;

public class CreateTeamRequest {

    @NotBlank
    private String teamName;

    @NotBlank
    private String practiceRegion;

    private String practiceNote;

    public CreateTeamRequest() {}

    public String getTeamName() { return teamName; }
    public String getPracticeRegion() { return practiceRegion; }
    public String getPracticeNote() { return practiceNote; }

    public void setTeamName(String teamName) { this.teamName = teamName; }
    public void setPracticeRegion(String practiceRegion) { this.practiceRegion = practiceRegion; }
    public void setPracticeNote(String practiceNote) { this.practiceNote = practiceNote; }
}
