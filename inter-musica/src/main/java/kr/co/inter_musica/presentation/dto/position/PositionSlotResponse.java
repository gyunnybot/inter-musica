package kr.co.inter_musica.presentation.dto.position;

import java.time.Instant;

public class PositionSlotResponse {
    private Long id;
    private Long teamId;
    private String instrument;
    private int capacity;
    private String requiredLevelMin;
    private Instant createdAt;

    public PositionSlotResponse() {}

    public PositionSlotResponse(Long id, Long teamId, String instrument, int capacity, String requiredLevelMin, Instant createdAt) {
        this.id = id;
        this.teamId = teamId;
        this.instrument = instrument;
        this.capacity = capacity;
        this.requiredLevelMin = requiredLevelMin;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getTeamId() { return teamId; }
    public String getInstrument() { return instrument; }
    public int getCapacity() { return capacity; }
    public String getRequiredLevelMin() { return requiredLevelMin; }
    public Instant getCreatedAt() { return createdAt; }
}
