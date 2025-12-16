package kr.co.inter_musica.presentation.dto.position;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreatePositionSlotRequest {

    @NotBlank
    private String instrument;

    @Min(1)
    private int capacity;

    @NotBlank
    private String requiredLevelMin;

    public CreatePositionSlotRequest() {}

    public String getInstrument() { return instrument; }
    public int getCapacity() { return capacity; }
    public String getRequiredLevelMin() { return requiredLevelMin; }

    public void setInstrument(String instrument) { this.instrument = instrument; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setRequiredLevelMin(String requiredLevelMin) { this.requiredLevelMin = requiredLevelMin; }
}
