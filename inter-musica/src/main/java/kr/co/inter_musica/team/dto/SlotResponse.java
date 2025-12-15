package kr.co.inter_musica.team.dto;

import kr.co.inter_musica.team.domain.enumm.Level;

public class SlotResponse {
    private Long slotId;
    private Long teamId;
    private String instrument;
    private int capacity;
    private Level requiredLevelMin;

    public SlotResponse() {}

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public Level getRequiredLevelMin() { return requiredLevelMin; }
    public void setRequiredLevelMin(Level requiredLevelMin) { this.requiredLevelMin = requiredLevelMin; }
}
