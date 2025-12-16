package kr.co.inter_musica.infrastructure.persistence.jpa;

import kr.co.inter_musica.infrastructure.persistence.entity.ProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileJpaRepository extends JpaRepository<ProfileJpaEntity, Long> {
}