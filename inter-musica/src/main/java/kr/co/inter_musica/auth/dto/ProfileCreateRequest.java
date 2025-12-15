package kr.co.inter_musica.auth.dto;

import kr.co.inter_musica.team.domain.enumm.Level;

public class ProfileCreateRequest {
    private String instrument;
    private Level level;
    private String region;

    public ProfileCreateRequest() {}

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
