package kr.co.inter_musica.team.service;

import kr.co.inter_musica.team.domain.enumm.JoinRequestStatus;
import kr.co.inter_musica.team.dto.JoinRequestWithProfileResponse;
import kr.co.inter_musica.team.dto.SlotCreateRequest;
import kr.co.inter_musica.team.dto.SlotResponse;
import kr.co.inter_musica.team.dto.SlotUpdateRequest;

import java.util.List;

public interface SlotService {
    SlotResponse createSlot(Long leaderUserId, Long teamId, SlotCreateRequest request);
    SlotResponse updateSlot(Long leaderUserId, Long teamId, Long slotId, SlotUpdateRequest request);
    void deleteSlot(Long leaderUserId, Long teamId, Long slotId);
    List<JoinRequestWithProfileResponse> getSlotJoinRequests(Long leaderUserId, Long teamId, Long slotId, JoinRequestStatus status);
}
