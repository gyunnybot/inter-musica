package kr.co.inter_musica.team.service;


import kr.co.inter_musica.team.domain.enumm.Level;
import kr.co.inter_musica.team.dto.*;

import java.util.List;

public interface TeamService {
    TeamResponse createTeam(Long leaderUserId, TeamCreateRequest request);
    TeamResponse updateTeam(Long leaderUserId, Long teamId, TeamUpdateRequest request);
    void deleteTeam(Long leaderUserId, Long teamId);
    TeamDetailResponse getTeamDetail(Long teamId);
    List<TeamSummaryResponse> searchTeams(String region, String instrument, Level level);
    List<MemberProfileResponse> getTeamMembers(Long teamId);
}