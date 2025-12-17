package kr.co.inter_musica.infrastructure.persistence.jpa;

import jakarta.persistence.LockModeType;
import kr.co.inter_musica.infrastructure.persistence.entity.PositionSlotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PositionSlotJpaRepository extends JpaRepository<PositionSlotJpaEntity, Long> {
    List<PositionSlotJpaEntity> findByTeamId(Long teamId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PositionSlotJpaEntity p where p.id = :id") // 메서드 실행 쿼리 고정 (메서드명으로 유추 방지)
    Optional<PositionSlotJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
