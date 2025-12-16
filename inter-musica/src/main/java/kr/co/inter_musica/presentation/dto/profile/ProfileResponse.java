package kr.co.inter_musica.presentation.dto.profile;

import java.time.Instant;

public class ProfileResponse {
    private Long userId;
    private String name;
    private String instrument;
    private String level;
    private String region;
    private Instant updatedAt;

    public ProfileResponse() {}

    public ProfileResponse(Long userId, String name, String instrument, String level, String region, Instant updatedAt) {
        this.userId = userId;
        this.name = name;
        this.instrument = instrument;
        this.level = level;
        this.region = region;
        this.updatedAt = updatedAt;
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getInstrument() { return instrument; }
    public String getLevel() { return level; }
    public String getRegion() { return region; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setInstrument(String instrument) { this.instrument = instrument; }
    public void setLevel(String level) { this.level = level; }
    public void setRegion(String region) { this.region = region; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
