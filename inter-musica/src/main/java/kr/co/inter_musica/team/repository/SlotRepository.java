package kr.co.inter_musica.team.repository;

import kr.co.inter_musica.team.domain.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {
    Optional<Slot> findByIdAndTeamId(Long slotId, Long teamId); // slot-team 검증
    List<Slot> findByTeamId(Long teamId);
}
