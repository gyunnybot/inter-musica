package kr.co.inter_musica.application;

import kr.co.inter_musica.domain.enums.Region;
import kr.co.inter_musica.infrastructure.persistence.entity.RegionJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.entity.TeamMemberJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.entity.TeamJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.jpa.RegionJpaRepository;
import kr.co.inter_musica.infrastructure.persistence.jpa.TeamMemberJpaRepository;
import kr.co.inter_musica.infrastructure.persistence.jpa.TeamJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TeamService {

    private final TeamJpaRepository teamJpaRepository;
    private final TeamMemberJpaRepository teamMemberJpaRepository;
    private final RegionJpaRepository regionJpaRepository;

    @Autowired
    public TeamService(
            TeamJpaRepository teamJpaRepository,
            TeamMemberJpaRepository teamMemberJpaRepository,
            RegionJpaRepository regionJpaRepository
    ) {
        this.teamJpaRepository = teamJpaRepository;
        this.teamMemberJpaRepository = teamMemberJpaRepository;
        this.regionJpaRepository = regionJpaRepository;
    }

    @Transactional
    public Long createTeam(long leaderUserId, String teamName, List<String> practiceRegionsRaw, String practiceNote, String practiceRegionRaw) {
        List<String> requestedRegions = practiceRegionsRaw;
        if (requestedRegions == null || requestedRegions.isEmpty()) {
            if (practiceRegionRaw != null && !practiceRegionRaw.isBlank()) {
                requestedRegions = List.of(practiceRegionRaw);
            } else {
                throw new IllegalArgumentException("지역 선택은 필수입니다.");
            }
        }

        List<String> regionCodes = requestedRegions.stream()
                .map(Region::from)
                .map(Region::name)
                .distinct()
                .toList();

        List<RegionJpaEntity> regions = resolveRegions(regionCodes);
        String practiceRegion = regions.get(0).getCode();

        TeamJpaEntity team = new TeamJpaEntity(leaderUserId, teamName, practiceRegion, practiceNote);
        team.setRegions(new LinkedHashSet<>(regions));
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
                .filter(teamJpaEntity -> hasRegion(teamJpaEntity, region))
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamJpaEntity findTeamById(Long teamId) {
        return teamJpaRepository.findById(teamId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<TeamJpaEntity> findMyTeams(long userId) {
        List<TeamMemberJpaEntity> memberships = teamMemberJpaRepository.findByUserIdOrderByJoinedAtDesc(userId);
        if (memberships.isEmpty()) {
            return List.of();
        }

        List<Long> teamIds = memberships.stream()
                .map(TeamMemberJpaEntity::getTeamId)
                .distinct()
                .toList();

        Map<Long, TeamJpaEntity> teamMap = new HashMap<>();
        teamJpaRepository.findAllById(teamIds).forEach(team -> teamMap.put(team.getId(), team));

        List<TeamJpaEntity> ordered = new ArrayList<>();
        for (Long teamId : teamIds) {
            TeamJpaEntity team = teamMap.get(teamId);
            if (team != null) {
                ordered.add(team);
            }
        }

        return ordered;
    }

    private List<RegionJpaEntity> resolveRegions(List<String> regionCodes) {
        List<RegionJpaEntity> regions = new ArrayList<>();
        for (String code : regionCodes) {
            RegionJpaEntity region = regionJpaRepository.findById(code)
                    .orElseGet(() -> regionJpaRepository.save(new RegionJpaEntity(code)));
            regions.add(region);
        }
        return regions;
    }

    private boolean hasRegion(TeamJpaEntity teamJpaEntity, String region) {
        Set<RegionJpaEntity> regions = teamJpaEntity.getRegions();
        if (regions != null && !regions.isEmpty()) {
            return regions.stream().anyMatch(r -> region.equals(r.getCode()));
        }
        return region.equals(teamJpaEntity.getPracticeRegion());
    }
}
