package kr.co.inter_musica.presentation.controller;

import kr.co.inter_musica.application.JoinRequestService;
import kr.co.inter_musica.domain.enums.JoinRequestStatus;
import kr.co.inter_musica.infrastructure.persistence.entity.JoinRequestJpaEntity;
import kr.co.inter_musica.domain.security.SecurityUtil;
import kr.co.inter_musica.presentation.dto.joinrequest.JoinRequestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class JoinRequestController {

    private final JoinRequestService joinService;

    @Autowired
    public JoinRequestController(JoinRequestService joinService) {
        this.joinService = joinService;
    }

    // 지원자 : 지원 (팀장은 지원할 수 없음. service 에서 filtering)
    @PostMapping("/teams/{teamId}/positions/{positionId}/join-requests")
    public ResponseEntity<Long> applyJoinRequest(
            @PathVariable Long teamId,
            @PathVariable Long positionId
    ) {
        long userId = SecurityUtil.currentUserId();

        Long joinRequestId = joinService.apply(userId, teamId, positionId);

        return ResponseEntity.ok(joinRequestId);
    }

    // 지원자 : 취소
    @PostMapping("/join-requests/{joinRequestId}/cancel")
    public ResponseEntity<Void> cancelJoinRequest(
            @PathVariable Long joinRequestId
    ) {
        long userId = SecurityUtil.currentUserId();

        joinService.cancel(userId, joinRequestId);

        return ResponseEntity.ok().build();
    }

    // 팀장 : 지원자 리스트 확인
    @GetMapping("/teams/{teamId}/positions/{positionId}/join-requests")
    public ResponseEntity<List<JoinRequestResponse>> getApplicantList(
            @PathVariable Long teamId,
            @PathVariable Long positionId,
            @RequestParam(required = false) JoinRequestStatus joinRequestStatus
    ) {
        long userId = SecurityUtil.currentUserId();

        List<JoinRequestJpaEntity> list = joinService.listApplicants(userId, teamId, positionId, joinRequestStatus);

        List<JoinRequestResponse> responseList = list.stream()
                .map(applicants -> new JoinRequestResponse(
                        applicants.getId(),
                        applicants.getTeamId(),
                        applicants.getPositionSlotId(),
                        applicants.getApplicantUserId(),
                        applicants.getStatus(),
                        applicants.getCreatedAt(),
                        applicants.getUpdatedAt()
                    )
                )
                .toList();

        return ResponseEntity.ok(responseList);
    }

    // 팀장 : 수락
    @PostMapping("/join-requests/{joinRequestId}/accept")
    public ResponseEntity<Void> acceptJoinRequest(
            @PathVariable Long joinRequestId
    ) {
        long userId = SecurityUtil.currentUserId();

        joinService.accept(userId, joinRequestId);

        return ResponseEntity.ok().build();
    }

    // 팀장 : 거절
    @PostMapping("/join-requests/{joinRequestId}/reject")
    public ResponseEntity<Void> rejectJoinRequest(
            @PathVariable Long joinRequestId
    ) {
        long userId = SecurityUtil.currentUserId();

        joinService.reject(userId, joinRequestId);

        return ResponseEntity.ok().build();
    }
}
