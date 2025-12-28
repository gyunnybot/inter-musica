package kr.co.inter_musica.presentation.dto.team;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalTime;

public class CreateTeamRequest {

    @NotBlank
    private String teamName;

    @NotBlank
    private String practiceRegion;

    private String practiceNote;
    private LocalTime coreTimeStart;

    private LocalTime coreTimeEnd;

    public CreateTeamRequest() {}

    public String getTeamName() { return teamName; }
    public String getPracticeRegion() { return practiceRegion; }
    public String getPracticeNote() { return practiceNote; }
    public LocalTime getCoreTimeStart() { return coreTimeStart; }
    public LocalTime getCoreTimeEnd() { return coreTimeEnd; }

    public void setTeamName(String teamName) { this.teamName = teamName; }
    public void setPracticeRegion(String practiceRegion) { this.practiceRegion = practiceRegion; }
    public void setPracticeNote(String practiceNote) { this.practiceNote = practiceNote; }
    public void setCoreTimeStart(LocalTime coreTimeStart) { this.coreTimeStart = coreTimeStart; }
    public void setCoreTimeEnd(LocalTime coreTimeEnd) { this.coreTimeEnd = coreTimeEnd; }
}
