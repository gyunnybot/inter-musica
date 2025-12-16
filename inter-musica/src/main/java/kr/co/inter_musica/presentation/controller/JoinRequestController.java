package kr.co.inter_musica.presentation.controller;

import kr.co.inter_musica.application.JoinRequestService;
import kr.co.inter_musica.domain.enums.JoinRequestStatus;
import kr.co.inter_musica.infrastructure.persistence.entity.JoinRequestJpaEntity;
import kr.co.inter_musica.presentation.security.SecurityUtil;
import kr.co.inter_musica.presentation.dto.joinrequest.JoinRequestResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class JoinRequestController {

    private final JoinRequestService joinService;

    public JoinRequestController(JoinRequestService joinService) {
        this.joinService = joinService;
    }

    // apply
    @PostMapping("/teams/{teamId}/positions/{positionId}/join-requests")
    public ResponseEntity<Long> apply(@PathVariable Long teamId, @PathVariable Long positionId) {
        long userId = SecurityUtil.currentUserId();
        Long joinRequestId = joinService.apply(userId, teamId, positionId);
        return ResponseEntity.ok(joinRequestId);
    }

    // cancel
    @PostMapping("/join-requests/{joinRequestId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long joinRequestId) {
        long userId = SecurityUtil.currentUserId();
        joinService.cancel(userId, joinRequestId);
        return ResponseEntity.ok().build();
    }

    // leader list
    @GetMapping("/teams/{teamId}/positions/{positionId}/join-requests")
    public ResponseEntity<List<JoinRequestResponse>> list(
            @PathVariable Long teamId,
            @PathVariable Long positionId,
            @RequestParam(required = false) JoinRequestStatus status
    ) {
        long userId = SecurityUtil.currentUserId();
        List<JoinRequestJpaEntity> list = joinService.listApplicants(userId, teamId, positionId, status);

        List<JoinRequestResponse> res = list.stream()
                .map(jr -> new JoinRequestResponse(
                        jr.getId(), jr.getTeamId(), jr.getPositionSlotId(), jr.getApplicantUserId(),
                        jr.getStatus(), jr.getCreatedAt(), jr.getUpdatedAt()
                ))
                .toList();

        return ResponseEntity.ok(res);
    }

    // accept/reject
    @PostMapping("/join-requests/{joinRequestId}/accept")
    public ResponseEntity<Void> accept(@PathVariable Long joinRequestId) {
        long userId = SecurityUtil.currentUserId();
        joinService.accept(userId, joinRequestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/join-requests/{joinRequestId}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long joinRequestId) {
        long userId = SecurityUtil.currentUserId();
        joinService.reject(userId, joinRequestId);
        return ResponseEntity.ok().build();
    }
}
