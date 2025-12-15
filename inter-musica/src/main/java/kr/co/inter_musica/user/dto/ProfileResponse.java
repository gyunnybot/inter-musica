package kr.co.inter_musica.user.dto;

import kr.co.inter_musica.team.domain.enumm.Level;

public class ProfileResponse {
    private Long userId;
    private String instrument;
    private Level level;
    private String region;

    public ProfileResponse() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
