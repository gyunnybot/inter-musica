package kr.co.inter_musica.team.repository;

import kr.co.inter_musica.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}