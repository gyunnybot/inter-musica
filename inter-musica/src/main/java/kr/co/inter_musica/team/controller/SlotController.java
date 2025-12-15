package kr.co.inter_musica.team.controller;

import kr.co.inter_musica.dto.ApiResponse;
import kr.co.inter_musica.team.domain.enumm.JoinRequestStatus;
import kr.co.inter_musica.team.dto.JoinRequestWithProfileResponse;
import kr.co.inter_musica.team.dto.SlotCreateRequest;
import kr.co.inter_musica.team.dto.SlotResponse;
import kr.co.inter_musica.team.dto.SlotUpdateRequest;
import kr.co.inter_musica.team.service.SlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams/{teamId}/slots")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SlotResponse>> createSlot(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long teamId,
            @RequestBody SlotCreateRequest request
    ) {
        SlotResponse res = slotService.createSlot(currentUserId, teamId, request);
        return ResponseEntity.status(201).body(new ApiResponse<SlotResponse>(res));
    }

    @PutMapping("/{slotId}")
    public ApiResponse<SlotResponse> updateSlot(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long teamId,
            @PathVariable Long slotId,
            @RequestBody SlotUpdateRequest request
    ) {
        return new ApiResponse<SlotResponse>(slotService.updateSlot(currentUserId, teamId, slotId, request));
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteSlot(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long teamId,
            @PathVariable Long slotId
    ) {
        slotService.deleteSlot(currentUserId, teamId, slotId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{slotId}/join-requests")
    public ApiResponse<List<JoinRequestWithProfileResponse>> getSlotJoinRequests(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long teamId,
            @PathVariable Long slotId,
            @RequestParam(required = false) JoinRequestStatus status
    ) {
        return new ApiResponse<List<JoinRequestWithProfileResponse>>(
                slotService.getSlotJoinRequests(currentUserId, teamId, slotId, status)
        );
    }
}
