package kr.co.inter_musica.team.dto;

import kr.co.inter_musica.team.domain.enumm.Level;

public class SlotUpdateRequest {
    private Integer capacity;
    private Level requiredLevelMin;

    public SlotUpdateRequest() {}

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Level getRequiredLevelMin() { return requiredLevelMin; }
    public void setRequiredLevelMin(Level requiredLevelMin) { this.requiredLevelMin = requiredLevelMin; }
}
