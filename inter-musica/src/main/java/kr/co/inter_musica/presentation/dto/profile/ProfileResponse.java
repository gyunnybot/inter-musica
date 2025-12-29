package kr.co.inter_musica.presentation.dto.profile;

import java.time.Instant;
import java.util.List;

public class ProfileResponse {
    private Long userId;
    private String name;
    private String instrument;
    private String level;
    private List<String> practiceRegions;
    private Instant updatedAt;

    public ProfileResponse() {}

    public ProfileResponse(Long userId, String name, String instrument, String level, List<String> practiceRegions, Instant updatedAt) {
        this.userId = userId;
        this.name = name;
        this.instrument = instrument;
        this.level = level;
        this.practiceRegions = practiceRegions;
        this.updatedAt = updatedAt;
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getInstrument() { return instrument; }
    public String getLevel() { return level; }
    public List<String> getRegion() { return practiceRegions; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setInstrument(String instrument) { this.instrument = instrument; }
    public void setLevel(String level) { this.level = level; }
    public void setRegion(String region) { this.practiceRegions = practiceRegions; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
