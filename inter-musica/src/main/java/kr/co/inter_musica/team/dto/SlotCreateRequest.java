package kr.co.inter_musica.team.dto;

import kr.co.inter_musica.team.domain.enumm.Level;

public class SlotCreateRequest {
    private String instrument;
    private int capacity;
    private Level requiredLevelMin;

    public SlotCreateRequest() {}

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public Level getRequiredLevelMin() { return requiredLevelMin; }
    public void setRequiredLevelMin(Level requiredLevelMin) { this.requiredLevelMin = requiredLevelMin; }
}
