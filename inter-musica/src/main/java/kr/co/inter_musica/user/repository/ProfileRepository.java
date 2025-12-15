package kr.co.inter_musica.user.repository;

import kr.co.inter_musica.user.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
