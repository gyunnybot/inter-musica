package kr.co.inter_musica.team.service;

import kr.co.inter_musica.team.domain.enumm.JoinRequestStatus;
import kr.co.inter_musica.team.dto.AcceptResponse;
import kr.co.inter_musica.team.dto.JoinRequestResponse;
import kr.co.inter_musica.team.dto.JoinRequestStatusResponse;
import kr.co.inter_musica.team.dto.MyJoinRequestResponse;

import java.util.List;

public interface JoinRequestService {
    JoinRequestResponse apply(Long userId, Long teamId, Long slotId);
    JoinRequestStatusResponse cancel(Long userId, Long joinRequestId);
    AcceptResponse accept(Long leaderUserId, Long joinRequestId);
    JoinRequestStatusResponse reject(Long leaderUserId, Long joinRequestId);
    List<MyJoinRequestResponse> getMyJoinRequests(Long userId, JoinRequestStatus status);
}
