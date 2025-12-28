package kr.co.inter_musica.infrastructure.persistence.jpa;

import kr.co.inter_musica.infrastructure.persistence.entity.TeamMemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberJpaRepository extends JpaRepository<TeamMemberJpaEntity, Long> {
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    List<TeamMemberJpaEntity> findByUserIdOrderByJoinedAtDesc(Long userId);

    Optional<TeamMemberJpaEntity> findTopByUserIdOrderByJoinedAtDesc(Long userId);
}
