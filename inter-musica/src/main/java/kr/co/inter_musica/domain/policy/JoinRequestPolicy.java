package kr.co.inter_musica.domain.policy;

import kr.co.inter_musica.domain.enums.JoinRequestStatus;
import kr.co.inter_musica.domain.enums.Level;
import kr.co.inter_musica.presentation.exception.ApiException;
import kr.co.inter_musica.presentation.exception.ErrorCode;

public class JoinRequestPolicy {
    public void validateRegionMatch(String profileRegion, String teamPracticeRegion) {
        if (!profileRegion.equals(teamPracticeRegion)) {
            throw new ApiException(ErrorCode.JOIN_REQUEST_REGION_MISMATCH, "프로필 지역과 팀 연습 지역이 일치하지 않습니다.");
        }
    }

    public void validateInstrumentMatch(String profileInstrument, String slotInstrument) {
        if (!profileInstrument.equals(slotInstrument)) {
            throw new ApiException(ErrorCode.JOIN_REQUEST_INSTRUMENT_MISMATCH, "프로필 악기와 포지션 악기가 일치하지 않습니다.");
        }
    }

    public void validateLevel(String profileLevelRaw, String requiredMinRaw) {
        Level profile = Level.from(profileLevelRaw);
        Level required = Level.from(requiredMinRaw);

        if (!profile.isAtLeast(required)) {
            throw new ApiException(ErrorCode.JOIN_REQUEST_LEVEL_MISMATCH, "요구 레벨을 만족하지 않습니다.");
        }
    }

    public void ensureCancelable(JoinRequestStatus status) {
        if (status != JoinRequestStatus.APPLIED) {
            throw new ApiException(ErrorCode.JOIN_REQUEST_CANNOT_CANCEL_NOT_APPLIED, "APPLIED 상태에서만 취소할 수 있습니다.");
        }
    }

    public void ensureDecidable(JoinRequestStatus status) {
        if (status != JoinRequestStatus.APPLIED) {
            throw new ApiException(ErrorCode.JOIN_REQUEST_CANNOT_DECIDE_NOT_APPLIED, "APPLIED 상태에서만 수락/거절할 수 있습니다.");
        }
    }

    public void ensureCapacityAvailable(long acceptedCount, int capacity) {
        if (acceptedCount >= capacity) {
            throw new ApiException(ErrorCode.POSITION_CAPACITY_FULL, "정원이 가득 찼습니다.");
        }
    }
}
