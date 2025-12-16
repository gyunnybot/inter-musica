package kr.co.inter_musica.presentation.dto.profile;

public class ProfileUpdateRequest {

    private String name;
    private String instrument;

    private String level;

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
