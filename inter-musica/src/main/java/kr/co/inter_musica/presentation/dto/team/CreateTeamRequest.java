package kr.co.inter_musica.presentation.dto.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalTime;
import java.util.List;

public class CreateTeamRequest {

    @NotBlank
    private String teamName;

    private String practiceRegion;

    @NotEmpty
    private List<@NotBlank String> practiceRegions;

    private String practiceNote;
    private LocalTime coreTimeStart;

    private LocalTime coreTimeEnd;

    public CreateTeamRequest() {}

    public String getTeamName() { return teamName; }
    public String getPracticeRegion() { return practiceRegion; }
    public List<String> getPracticeRegions() { return practiceRegions; }
    public String getPracticeNote() { return practiceNote; }
    public LocalTime getCoreTimeStart() { return coreTimeStart; }
    public LocalTime getCoreTimeEnd() { return coreTimeEnd; }

    public void setTeamName(String teamName) { this.teamName = teamName; }
    public void setPracticeRegion(String practiceRegion) { this.practiceRegion = practiceRegion; }
    public void setPracticeRegions(List<String> practiceRegions) { this.practiceRegions = practiceRegions; }
    public void setPracticeNote(String practiceNote) { this.practiceNote = practiceNote; }
    public void setCoreTimeStart(LocalTime coreTimeStart) { this.coreTimeStart = coreTimeStart; }
    public void setCoreTimeEnd(LocalTime coreTimeEnd) { this.coreTimeEnd = coreTimeEnd; }
}
