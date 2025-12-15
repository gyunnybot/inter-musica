package kr.co.inter_musica.team.controller;

import kr.co.inter_musica.dto.ApiResponse;
import kr.co.inter_musica.team.dto.AcceptResponse;
import kr.co.inter_musica.team.dto.JoinRequestResponse;
import kr.co.inter_musica.team.dto.JoinRequestStatusResponse;
import kr.co.inter_musica.team.service.JoinRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JoinRequestController {

    private final JoinRequestService joinRequestService;

    public JoinRequestController(JoinRequestService joinRequestService) {
        this.joinRequestService = joinRequestService;
    }

    @PostMapping("/teams/{teamId}/slots/{slotId}/join-requests")
    public ResponseEntity<ApiResponse<JoinRequestResponse>> apply(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long teamId,
            @PathVariable Long slotId
    ) {
        JoinRequestResponse res = joinRequestService.apply(currentUserId, teamId, slotId);
        return ResponseEntity.status(201).body(new ApiResponse<JoinRequestResponse>(res));
    }

    @PostMapping("/join-requests/{joinRequestId}/cancel")
    public ApiResponse<JoinRequestStatusResponse> cancel(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long joinRequestId
    ) {
        return new ApiResponse<JoinRequestStatusResponse>(joinRequestService.cancel(currentUserId, joinRequestId));
    }

    @PostMapping("/join-requests/{joinRequestId}/accept")
    public ApiResponse<AcceptResponse> accept(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long joinRequestId
    ) {
        return new ApiResponse<AcceptResponse>(joinRequestService.accept(currentUserId, joinRequestId));
    }

    @PostMapping("/join-requests/{joinRequestId}/reject")
    public ApiResponse<JoinRequestStatusResponse> reject(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long joinRequestId
    ) {
        return new ApiResponse<JoinRequestStatusResponse>(joinRequestService.reject(currentUserId, joinRequestId));
    }
}
