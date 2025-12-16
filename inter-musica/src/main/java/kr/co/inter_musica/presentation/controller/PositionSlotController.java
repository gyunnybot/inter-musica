package kr.co.inter_musica.presentation.controller;

import jakarta.validation.Valid;
import kr.co.inter_musica.application.PositionSlotService;
import kr.co.inter_musica.infrastructure.persistence.entity.PositionSlotJpaEntity;
import kr.co.inter_musica.presentation.security.SecurityUtil;
import kr.co.inter_musica.presentation.dto.position.CreatePositionSlotRequest;
import kr.co.inter_musica.presentation.dto.position.PositionSlotResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams/{teamId}/positions")
public class PositionSlotController {

    private final PositionSlotService positionService;

    public PositionSlotController(PositionSlotService positionService) {
        this.positionService = positionService;
    }

    @PostMapping
    public ResponseEntity<Long> create(@PathVariable Long teamId, @Valid @RequestBody CreatePositionSlotRequest req) {
        long userId = SecurityUtil.currentUserId();
        Long positionId = positionService.createSlot(userId, teamId, req.getInstrument(), req.getCapacity(), req.getRequiredLevelMin());
        return ResponseEntity.ok(positionId);
    }

    @GetMapping
    public ResponseEntity<List<PositionSlotResponse>> list(@PathVariable Long teamId) {
        List<PositionSlotJpaEntity> slots = positionService.listSlots(teamId);
        List<PositionSlotResponse> res = slots.stream()
                .map(p -> new PositionSlotResponse(p.getId(), p.getTeamId(), p.getInstrument(), p.getCapacity(), p.getRequiredLevelMin(), p.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(res);
    }
}
