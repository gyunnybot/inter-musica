package kr.co.inter_musica.presentation.dto.profile;

import jakarta.validation.constraints.NotBlank;

public class ProfileUpdateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String instrument;

    @NotBlank
    private String level;

    @NotBlank
    private String region;

    public ProfileUpdateRequest() {}

    public String getName() { return name; }
    public String getInstrument() { return instrument; }
    public String getLevel() { return level; }
    public String getRegion() { return region; }

    public void setName(String name) { this.name = name; }
    public void setInstrument(String instrument) { this.instrument = instrument; }
    public void setLevel(String level) { this.level = level; }
    public void setRegion(String region) { this.region = region; }
}
