package kr.co.inter_musica.presentation.dto.position;

public class PositionSlotStatResult {

    private Long positionSlotId;
    private long occupiedCount;

    public PositionSlotStatResult() {
    }

    public PositionSlotStatResult(Long positionSlotId, long occupiedCount) {
        this.positionSlotId = positionSlotId;
        this.occupiedCount = occupiedCount;
    }

    public Long getPositionSlotId() {
        return positionSlotId;
    }

    public void setPositionSlotId(Long positionSlotId) {
        this.positionSlotId = positionSlotId;
    }

    public long getOccupiedCount() {
        return occupiedCount;
    }

    public void setOccupiedCount(long occupiedCount) {
        this.occupiedCount = occupiedCount;
    }
}
