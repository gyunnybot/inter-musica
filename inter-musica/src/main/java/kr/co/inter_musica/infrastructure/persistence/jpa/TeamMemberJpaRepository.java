package kr.co.inter_musica.infrastructure.persistence.jpa;

import kr.co.inter_musica.infrastructure.persistence.entity.TeamMemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberJpaRepository extends JpaRepository<TeamMemberJpaEntity, Long> {
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
}
