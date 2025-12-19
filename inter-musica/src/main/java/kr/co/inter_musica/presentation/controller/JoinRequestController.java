package kr.co.inter_musica.presentation.controller;

import kr.co.inter_musica.application.JoinRequestService;
import kr.co.inter_musica.domain.enums.JoinRequestStatus;
import kr.co.inter_musica.infrastructure.persistence.entity.JoinRequestJpaEntity;
import kr.co.inter_musica.domain.security.SecurityUtil;
import kr.co.inter_musica.infrastructure.persistence.entity.PositionSlotJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.entity.TeamJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.jpa.PositionSlotJpaRepository;
import kr.co.inter_musica.infrastructure.persistence.jpa.TeamJpaRepository;
import kr.co.inter_musica.presentation.dto.joinrequest.JoinRequestResponse;
import kr.co.inter_musica.presentation.dto.joinrequest.MyJoinRequestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class JoinRequestController {

    private final JoinRequestService joinService;
    private final TeamJpaRepository teamJpaRepository;
    private final PositionSlotJpaRepository positionSlotJpaRepository;

    @Autowired
    public JoinRequestController(
            JoinRequestService joinService,
            TeamJpaRepository teamJpaRepository,
            PositionSlotJpaRepository positionSlotJpaRepository
    ) {
        this.joinService = joinService;
        this.teamJpaRepository = teamJpaRepository;
        this.positionSlotJpaRepository = positionSlotJpaRepository;
    }

    // 지원자 : 지원 (팀장은 지원할 수 없음. service 에서 filtering)
    @PostMapping("/teams/{teamId}/positions/{positionId}/join-requests")
    public ResponseEntity<Long> applyJoinRequest(
            @PathVariable Long teamId,
            @PathVariable Long positionId
    ) {
        long userId = SecurityUtil.currentUserId();

        Long joinRequestId = joinService.applyJoinRequest(userId, teamId, positionId);

        return ResponseEntity.ok(joinRequestId);
    }

    // 지원자 : 취소
    @PostMapping("/join-requests/{joinRequestId}/cancel")
    public ResponseEntity<Void> cancelJoinRequest(
            @PathVariable Long joinRequestId
    ) {
        long userId = SecurityUtil.currentUserId();

        joinService.cancelJoinRequest(userId, joinRequestId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/join-requests/me")
    public ResponseEntity<List<MyJoinRequestResponse>> myJoinRequests(
            @RequestParam(required = false) JoinRequestStatus status
    ) {
        long userId = SecurityUtil.currentUserId();

        List<JoinRequestJpaEntity> list = joinService.getMyJoinRequests(userId, status);
        if (list.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Map<Long, TeamJpaEntity> teamMap = teamJpaRepository.findAllById(
                list.stream().map(JoinRequestJpaEntity::getTeamId).distinct().toList()
        ).stream().collect(Collectors.toMap(TeamJpaEntity::getId, Function.identity()));

        Map<Long, PositionSlotJpaEntity> slotMap = positionSlotJpaRepository.findAllById(
                list.stream().map(JoinRequestJpaEntity::getPositionSlotId).distinct().toList()
        ).stream().collect(Collectors.toMap(PositionSlotJpaEntity::getId, Function.identity()));
        List<MyJoinRequestResponse> res = list.stream().map(jr -> {
            TeamJpaEntity t = teamMap.get(jr.getTeamId());
            PositionSlotJpaEntity s = slotMap.get(jr.getPositionSlotId());

            MyJoinRequestResponse.TeamSummary team = new MyJoinRequestResponse.TeamSummary(jr.getTeamId(),
                    (t == null ? "(삭제된 팀)" : t.getTeamName()),
                    (t == null ? "-" : t.getPracticeRegion())
            );

            MyJoinRequestResponse.PositionSummary position = new MyJoinRequestResponse.PositionSummary(
                    jr.getPositionSlotId(),
                    (s == null ? "-" : s.getInstrument()),
                    (s == null ? "-" : s.getRequiredLevelMin()),
                    (s == null ? 0 : s.getCapacity())
            );

            boolean cancellable = (jr.getStatus() == JoinRequestStatus.APPLIED);

            return new MyJoinRequestResponse(
                    jr.getId(),
                    jr.getStatus(),
                    jr.getCreatedAt(),
                    jr.getUpdatedAt(),
                    team,
                    position,
                    cancellable
            );
        }).toList();

        return ResponseEntity.ok(res);
    }

    // 팀장 : 지원자 리스트 확인
    @GetMapping("/teams/{teamId}/positions/{positionId}/join-requests")
    public ResponseEntity<List<JoinRequestResponse>> getApplicantList(
            @PathVariable Long teamId,
            @PathVariable Long positionId,
            @RequestParam(required = false) JoinRequestStatus joinRequestStatus
    ) {
        long userId = SecurityUtil.currentUserId();

        List<JoinRequestJpaEntity> list = joinService.getApplicantList(userId, teamId, positionId, joinRequestStatus);

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

        joinService.acceptJoinRequest(userId, joinRequestId);

        return ResponseEntity.ok().build();
    }

    // 팀장 : 거절
    @PostMapping("/join-requests/{joinRequestId}/reject")
    public ResponseEntity<Void> rejectJoinRequest(
            @PathVariable Long joinRequestId
    ) {
        long userId = SecurityUtil.currentUserId();

        joinService.rejectJoinRequest(userId, joinRequestId);

        return ResponseEntity.ok().build();
    }
}
