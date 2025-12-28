package kr.co.inter_musica.infrastructure.persistence.jpa;

import kr.co.inter_musica.infrastructure.persistence.entity.RegionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionJpaRepository extends JpaRepository<RegionJpaEntity, String> {
}
