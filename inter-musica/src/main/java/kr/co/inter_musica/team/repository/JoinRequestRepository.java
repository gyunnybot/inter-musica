package kr.co.inter_musica.team.repository;

import kr.co.inter_musica.team.domain.JoinRequest;
import kr.co.inter_musica.team.domain.enumm.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    boolean existsBySlotIdAndApplicantUserIdAndStatus(Long slotId, Long applicantUserId, JoinRequestStatus status);

    List<JoinRequest> findByApplicantUserIdOrderByCreatedAtDesc(Long applicantUserId);

    List<JoinRequest> findByApplicantUserIdAndStatusOrderByCreatedAtDesc(Long applicantUserId, JoinRequestStatus status);

    List<JoinRequest> findBySlotIdAndStatus(Long slotId, JoinRequestStatus status);
}
