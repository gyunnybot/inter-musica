package kr.co.inter_musica.team.dto;

import kr.co.inter_musica.team.domain.enumm.Level;

public class SlotSummaryResponse {
    private Long slotId;
    private String instrument;
    private int capacity;
    private Level requiredLevelMin;
    private int activeMemberCount;

    public SlotSummaryResponse() {}

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public Level getRequiredLevelMin() { return requiredLevelMin; }
    public void setRequiredLevelMin(Level requiredLevelMin) { this.requiredLevelMin = requiredLevelMin; }

    public int getActiveMemberCount() { return activeMemberCount; }
    public void setActiveMemberCount(int activeMemberCount) { this.activeMemberCount = activeMemberCount; }
}
