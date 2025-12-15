package kr.co.inter_musica.user.controller;

import kr.co.inter_musica.auth.service.ProfileService;
import kr.co.inter_musica.dto.ApiResponse;
import kr.co.inter_musica.team.domain.enumm.JoinRequestStatus;
import kr.co.inter_musica.team.dto.MyJoinRequestResponse;
import kr.co.inter_musica.team.service.JoinRequestService;
import kr.co.inter_musica.user.dto.ProfileResponse;
import kr.co.inter_musica.user.dto.ProfileUpdateRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me")
public class MeController {

    private final ProfileService profileService;
    private final JoinRequestService joinRequestService;

    public MeController(ProfileService profileService, JoinRequestService joinRequestService) {
        this.profileService = profileService;
        this.joinRequestService = joinRequestService;
    }

    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> getMyProfile(@RequestAttribute("currentUserId") Long currentUserId) {
        return new ApiResponse<ProfileResponse>(profileService.getMyProfile(currentUserId));
    }

    @PutMapping("/profile")
    public ApiResponse<ProfileResponse> updateMyProfile(
            @RequestAttribute("currentUserId") Long currentUserId,
            @RequestBody ProfileUpdateRequest request
    ) {
        return new ApiResponse<ProfileResponse>(profileService.updateMyProfile(currentUserId, request));
    }

    @GetMapping("/join-requests")
    public ApiResponse<List<MyJoinRequestResponse>> getMyJoinRequests(
            @RequestAttribute("currentUserId") Long currentUserId,
            @RequestParam(name = "status", required = false) JoinRequestStatus status
    ) {
        return new ApiResponse<List<MyJoinRequestResponse>>(joinRequestService.getMyJoinRequests(currentUserId, status));
    }
}
