package kr.co.inter_musica.application;

import kr.co.inter_musica.domain.enums.Region;
import kr.co.inter_musica.infrastructure.persistence.entity.TeamJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.jpa.TeamJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamService {

    private final TeamJpaRepository teamRepo;

    public TeamService(TeamJpaRepository teamRepo) {
        this.teamRepo = teamRepo;
    }

    @Transactional
    public Long createTeam(long leaderUserId, String teamName, String practiceRegionRaw, String practiceNote) {
        // 도메인 enum 강제
        String practiceRegion = Region.from(practiceRegionRaw).name();

        TeamJpaEntity team = new TeamJpaEntity(leaderUserId, teamName, practiceRegion, practiceNote);
        teamRepo.save(team);
        return team.getId();
    }

    @Transactional(readOnly = true)
    public List<TeamJpaEntity> listTeams(String regionRaw) {
        // MVP: region 필터만 최소 지원. (instrument/levelMin 검색은 다음 단계에서 확장 추천)
        if (regionRaw == null || regionRaw.isBlank()) {
            return teamRepo.findAll();
        }
        String region = Region.from(regionRaw).name();

        // 간단히 전체 로드 후 필터(초기 MVP). 데이터 늘면 쿼리로 바꾸자.
        return teamRepo.findAll().stream()
                .filter(t -> region.equals(t.getPracticeRegion()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamJpaEntity getTeam(Long teamId) {
        return teamRepo.findById(teamId).orElse(null);
    }
}
