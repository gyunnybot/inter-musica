package kr.co.inter_musica.presentation.controller;

import jakarta.validation.Valid;
import kr.co.inter_musica.application.TeamService;
import kr.co.inter_musica.infrastructure.persistence.entity.TeamJpaEntity;
import kr.co.inter_musica.presentation.exception.ApiException;
import kr.co.inter_musica.presentation.exception.ErrorCode;
import kr.co.inter_musica.presentation.security.SecurityUtil;
import kr.co.inter_musica.presentation.dto.team.CreateTeamRequest;
import kr.co.inter_musica.presentation.dto.team.CreateTeamResponse;
import kr.co.inter_musica.presentation.dto.team.TeamSummaryResponse;
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
    public ResponseEntity<CreateTeamResponse> create(@Valid @RequestBody CreateTeamRequest req) {
        long leaderUserId = SecurityUtil.currentUserId();
        Long teamId = teamService.createTeam(leaderUserId, req.getTeamName(), req.getPracticeRegion(), req.getPracticeNote());
        return ResponseEntity.ok(new CreateTeamResponse(teamId));
    }

    @GetMapping
    public ResponseEntity<List<TeamSummaryResponse>> list(@RequestParam(required = false) String region) {
        List<TeamJpaEntity> teams = teamService.listTeams(region);
        List<TeamSummaryResponse> res = teams.stream()
                .map(t -> new TeamSummaryResponse(t.getId(), t.getTeamName(), t.getPracticeRegion(), t.getPracticeNote(), t.getLeaderUserId(), t.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamSummaryResponse> get(@PathVariable Long teamId) {
        TeamJpaEntity team = teamService.getTeam(teamId);
        if (team == null) throw new ApiException(ErrorCode.TEAM_NOT_FOUND, "팀을 찾을 수 없습니다.");
        return ResponseEntity.ok(new TeamSummaryResponse(team.getId(), team.getTeamName(), team.getPracticeRegion(), team.getPracticeNote(), team.getLeaderUserId(), team.getCreatedAt()));
    }
}
