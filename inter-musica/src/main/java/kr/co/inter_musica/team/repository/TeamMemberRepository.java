package kr.co.inter_musica.team.repository;

import kr.co.inter_musica.team.domain.TeamMember;
import kr.co.inter_musica.team.domain.enumm.TeamMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    int countBySlotIdAndStatus(Long slotId, TeamMemberStatus status);

    boolean existsByUserIdAndStatus(Long userId, TeamMemberStatus status); // 동시에 1팀 정책

    boolean existsByTeamIdAndStatus(Long teamId, TeamMemberStatus status); // 팀 삭제 정책

    List<TeamMember> findByTeamIdAndStatus(Long teamId, TeamMemberStatus status);
}
