package kr.co.inter_musica.user.dto;

import kr.co.inter_musica.team.domain.enumm.Level;

public class ProfileUpdateRequest {
    private String instrument;
    private Level level;
    private String region;

    public ProfileUpdateRequest() {}

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
