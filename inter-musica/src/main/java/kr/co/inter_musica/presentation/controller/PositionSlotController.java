package kr.co.inter_musica.presentation.controller;

import jakarta.validation.Valid;
import kr.co.inter_musica.application.PositionSlotService;
import kr.co.inter_musica.infrastructure.persistence.entity.PositionSlotJpaEntity;
import kr.co.inter_musica.domain.security.SecurityUtil;
import kr.co.inter_musica.presentation.dto.position.CreatePositionSlotRequest;
import kr.co.inter_musica.presentation.dto.position.PositionSlotResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams/{teamId}/positions")
public class PositionSlotController {

    private final PositionSlotService positionService;

    @Autowired
    public PositionSlotController(PositionSlotService positionService) {
        this.positionService = positionService;
    }

    // 팀장 : 포지션 슬롯 생성
    @PostMapping
    public ResponseEntity<Long> createPositionSlot(
            @PathVariable Long teamId,
            @Valid @RequestBody CreatePositionSlotRequest createPositionSlotRequest
    ) {
        long userId = SecurityUtil.currentUserId();

        Long positionId = positionService.createPositionSlot(
                userId,
                teamId,
                createPositionSlotRequest.getInstrument(),
                createPositionSlotRequest.getCapacity(),
                createPositionSlotRequest.getRequiredLevelMin()
        );

        return ResponseEntity.ok(positionId);
    }

    // 공통 : 포지션 슬롯 조회
    @GetMapping
    public ResponseEntity<List<PositionSlotResponse>> getPositionSlotList(
            @PathVariable Long teamId
    ) {
        List<PositionSlotJpaEntity> list = positionService.getPositionSlotList(teamId);

        List<PositionSlotResponse> responseList = list.stream()
                .map(position -> new PositionSlotResponse(
                        position.getId(),
                        position.getTeamId(),
                        position.getInstrument(),
                        position.getCapacity(),
                        position.getRequiredLevelMin(),
                        position.getCreatedAt()
                    )
                )
                .toList();

        return ResponseEntity.ok(responseList);
    }
}
