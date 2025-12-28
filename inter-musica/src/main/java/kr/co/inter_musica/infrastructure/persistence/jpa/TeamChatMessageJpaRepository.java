package kr.co.inter_musica.infrastructure.persistence.jpa;

import kr.co.inter_musica.infrastructure.persistence.entity.TeamChatMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamChatMessageJpaRepository extends JpaRepository<TeamChatMessageJpaEntity, Long> {
    List<TeamChatMessageJpaEntity> findTop50ByTeamIdOrderByCreatedAtDesc(Long teamId);
}
