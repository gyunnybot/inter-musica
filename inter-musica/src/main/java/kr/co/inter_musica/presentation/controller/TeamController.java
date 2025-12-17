package kr.co.inter_musica.presentation.controller;

import jakarta.validation.Valid;
import kr.co.inter_musica.application.TeamService;
import kr.co.inter_musica.infrastructure.persistence.entity.TeamJpaEntity;
import kr.co.inter_musica.domain.exception.ApiException;
import kr.co.inter_musica.domain.enums.ErrorCode;
import kr.co.inter_musica.domain.security.SecurityUtil;
import kr.co.inter_musica.presentation.dto.team.CreateTeamRequest;
import kr.co.inter_musica.presentation.dto.team.CreateTeamResponse;
import kr.co.inter_musica.presentation.dto.team.TeamSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<CreateTeamResponse> createTeam(
            @Valid @RequestBody CreateTeamRequest createTeamRequest
    ) {
        long leaderUserId = SecurityUtil.currentUserId();

        Long teamId = teamService.createTeam(
                leaderUserId,
                createTeamRequest.getTeamName(),
                createTeamRequest.getPracticeRegion(),
                createTeamRequest.getPracticeNote()
        );

        return ResponseEntity.ok(new CreateTeamResponse(teamId));
    }

    @GetMapping
    public ResponseEntity<List<TeamSummaryResponse>> getTeamList(
            @RequestParam(required = false) String region
    ) {
        List<TeamJpaEntity> list = teamService.listTeams(region);

        List<TeamSummaryResponse> responseList = list.stream()
                .map(team -> new TeamSummaryResponse(
                        team.getId(),
                        team.getTeamName(),
                        team.getPracticeRegion(),
                        team.getPracticeNote(),
                        team.getLeaderUserId(),
                        team.getCreatedAt()
                    )
                )
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamSummaryResponse> findTeamById(
            @PathVariable Long teamId
    ) {
        TeamJpaEntity team = teamService.getTeam(teamId);

        if (team == null) {
            throw new ApiException(ErrorCode.TEAM_NOT_FOUND, "팀을 찾을 수 없습니다.");
        }

        TeamSummaryResponse teamSummaryResponse = new TeamSummaryResponse(
                team.getId(),
                team.getTeamName(),
                team.getPracticeRegion(),
                team.getPracticeNote(),
                team.getLeaderUserId(),
                team.getCreatedAt()
        );

        return ResponseEntity.ok(teamSummaryResponse);
    }
}
