package kr.co.inter_musica.infrastructure.persistence.jpa;

import kr.co.inter_musica.domain.enums.JoinRequestStatus;
import kr.co.inter_musica.infrastructure.persistence.entity.JoinRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JoinRequestJpaRepository extends JpaRepository<JoinRequestJpaEntity, Long> {

    boolean existsByTeamIdAndPositionSlotIdAndApplicantUserIdAndStatus(
            Long teamId, Long positionSlotId, Long applicantUserId, JoinRequestStatus status
    );

    long countByPositionSlotIdAndStatus(Long positionSlotId, JoinRequestStatus status);

    long countByPositionSlotIdAndStatusIn(Long positionSlotId, List<JoinRequestStatus> statuses);

    List<JoinRequestJpaEntity> findByTeamIdAndPositionSlotIdAndStatus(Long teamId, Long positionSlotId, JoinRequestStatus status);

    List<JoinRequestJpaEntity> findByTeamIdAndPositionSlotId(Long teamId, Long positionSlotId);

    List<JoinRequestJpaEntity> findByApplicantUserIdOrderByCreatedAtDesc(Long applicantUserId);

    List<JoinRequestJpaEntity> findByApplicantUserIdAndStatusOrderByCreatedAtDesc(
            Long applicantUserId,
            JoinRequestStatus status
    );
}
