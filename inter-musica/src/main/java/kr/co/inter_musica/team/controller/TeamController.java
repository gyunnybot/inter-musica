package kr.co.inter_musica.team.controller;

import kr.co.inter_musica.dto.ApiResponse;
import kr.co.inter_musica.team.domain.enumm.Level;
import kr.co.inter_musica.team.dto.*;
import kr.co.inter_musica.team.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
            @RequestAttribute("currentUserId") Long currentUserId,
            @RequestBody TeamCreateRequest request
    ) {
        TeamResponse res = teamService.createTeam(currentUserId, request);
        return ResponseEntity.status(201).body(new ApiResponse<TeamResponse>(res));
    }

    @PutMapping("/{teamId}")
    public ApiResponse<TeamResponse> updateTeam(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long teamId,
            @RequestBody TeamUpdateRequest request
    ) {
        return new ApiResponse<TeamResponse>(teamService.updateTeam(currentUserId, teamId, request));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long teamId
    ) {
        teamService.deleteTeam(currentUserId, teamId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{teamId}")
    public ApiResponse<TeamDetailResponse> getTeamDetail(@PathVariable Long teamId) {
        return new ApiResponse<TeamDetailResponse>(teamService.getTeamDetail(teamId));
    }

    @GetMapping
    public ApiResponse<List<TeamSummaryResponse>> searchTeams(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String instrument,
            @RequestParam(required = false) Level level
    ) {
        return new ApiResponse<List<TeamSummaryResponse>>(teamService.searchTeams(region, instrument, level));
    }

    @GetMapping("/{teamId}/members")
    public ApiResponse<List<MemberProfileResponse>> getTeamMembers(@PathVariable Long teamId) {
        return new ApiResponse<List<MemberProfileResponse>>(teamService.getTeamMembers(teamId));
    }
}
