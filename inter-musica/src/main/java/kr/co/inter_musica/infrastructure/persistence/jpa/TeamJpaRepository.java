package kr.co.inter_musica.infrastructure.persistence.jpa;

import kr.co.inter_musica.infrastructure.persistence.entity.TeamJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamJpaRepository extends JpaRepository<TeamJpaEntity, Long> {
    boolean existsByLeaderUserId(Long leaderUserId);
}
