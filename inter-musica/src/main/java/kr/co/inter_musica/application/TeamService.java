package kr.co.inter_musica.application;

import kr.co.inter_musica.domain.enums.Region;
import kr.co.inter_musica.infrastructure.persistence.entity.TeamMemberJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.entity.TeamJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.jpa.TeamMemberJpaRepository;
import kr.co.inter_musica.infrastructure.persistence.jpa.TeamJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    private final TeamJpaRepository teamJpaRepository;
    private final TeamMemberJpaRepository teamMemberJpaRepository;

    @Autowired
    public TeamService(TeamJpaRepository teamJpaRepository, TeamMemberJpaRepository teamMemberJpaRepository) {
        this.teamJpaRepository = teamJpaRepository;
        this.teamMemberJpaRepository = teamMemberJpaRepository;
    }

    @Transactional
    public Long createTeam(long leaderUserId, String teamName, String practiceRegionRaw, String practiceNote,
                           LocalTime coreTimeStart, LocalTime coreTimeEnd) {
        String practiceRegion = Region.from(practiceRegionRaw).name();

        TeamJpaEntity team = new TeamJpaEntity(
                leaderUserId,
                teamName,
                practiceRegion,
                practiceNote,
                coreTimeStart,
                coreTimeEnd
        );

        teamJpaRepository.save(team);

        // 팀장도 팀에 포함
        teamMemberJpaRepository.save(new TeamMemberJpaEntity(
                    team.getId(),
                    leaderUserId
                )
        );

        return team.getId();
    }

    @Transactional(readOnly = true)
    public List<TeamJpaEntity> getTeamList(String regionRaw) {
        if (regionRaw == null || regionRaw.isBlank()) {
            return teamJpaRepository.findAll();
        }

        String region = Region.from(regionRaw).name();

        return teamJpaRepository.findAll().stream()
                .filter(teamJpaEntity -> region.equals(teamJpaEntity.getPracticeRegion()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamJpaEntity findTeamById(Long teamId) {
        return teamJpaRepository.findById(teamId).orElse(null);
    }

    @Transactional(readOnly = true)
    public TeamJpaEntity findMyTeam(long userId) {
        Optional<TeamJpaEntity> leaderTeam = teamJpaRepository.findTopByLeaderUserIdOrderByCreatedAtDesc(userId);

        if (leaderTeam.isPresent()) {
            return leaderTeam.get();
        }

        Optional<TeamMemberJpaEntity> membership = teamMemberJpaRepository.findTopByUserIdOrderByJoinedAtDesc(userId);

        if (membership.isEmpty()) {
            return null;
        }

        return teamJpaRepository.findById(membership.get().getTeamId()).orElse(null);
    }
}
