package kr.co.inter_musica.application;

import kr.co.inter_musica.domain.enums.Region;
import kr.co.inter_musica.infrastructure.persistence.entity.TeamJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.jpa.TeamJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamService {

    private final TeamJpaRepository teamJpaRepository;

    @Autowired
    public TeamService(TeamJpaRepository teamJpaRepository) {
        this.teamJpaRepository = teamJpaRepository;
    }

    @Transactional
    public Long createTeam(long leaderUserId, String teamName, String practiceRegionRaw, String practiceNote) {
        String practiceRegion = Region.from(practiceRegionRaw).name();

        TeamJpaEntity team = new TeamJpaEntity(leaderUserId, teamName, practiceRegion, practiceNote);
        teamJpaRepository.save(team);

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
}
