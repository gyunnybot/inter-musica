package kr.co.inter_musica.presentation.dto.position;

public class PositionSlotStatResponse {
    private Long positionId;
    private long acceptedCount;

    public PositionSlotStatResponse() {
    }

    public PositionSlotStatResponse(Long positionId, long acceptedCount) {
        this.positionId = positionId;
        this.acceptedCount = acceptedCount;
    }

    public Long getPositionId() {
        return positionId;
    }

    public long getAcceptedCount() {
        return acceptedCount;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public void setAcceptedCount(long acceptedCount) {
        this.acceptedCount = acceptedCount;
    }
}
