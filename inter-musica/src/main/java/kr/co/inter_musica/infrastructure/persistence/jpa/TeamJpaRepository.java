package kr.co.inter_musica.infrastructure.persistence.jpa;

import kr.co.inter_musica.infrastructure.persistence.entity.TeamJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamJpaRepository extends JpaRepository<TeamJpaEntity, Long> {
    boolean existsByLeaderUserId(Long leaderUserId);

    Optional<TeamJpaEntity> findTopByLeaderUserIdOrderByCreatedAtDesc(Long leaderUserId);
}
