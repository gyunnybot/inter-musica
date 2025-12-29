package kr.co.inter_musica.presentation.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import kr.co.inter_musica.domain.enums.Region;

import java.util.List;

public class ProfileUpdateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String instrument;

    @NotBlank
    private String level;

    @NotEmpty
    private List<String> practiceRegions;

    public ProfileUpdateRequest() {}

    public String getName() { return name; }
    public String getInstrument() { return instrument; }
    public String getLevel() { return level; }
    public List<String> getPracticeRegions() { return practiceRegions; }

    public void setName(String name) { this.name = name; }
    public void setInstrument(String instrument) { this.instrument = instrument; }
    public void setLevel(String level) { this.level = level; }
    public void setPracticeRegions(List<String> practiceRegions) { this.practiceRegions = practiceRegions; }
}
