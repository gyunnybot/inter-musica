package kr.co.inter_musica.application;

import kr.co.inter_musica.domain.enums.Instrument;
import kr.co.inter_musica.domain.enums.Level;
import kr.co.inter_musica.domain.enums.Region;
import kr.co.inter_musica.infrastructure.persistence.entity.ProfileJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.jpa.ProfileJpaRepository;
import kr.co.inter_musica.presentation.exception.ApiException;
import kr.co.inter_musica.presentation.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final ProfileJpaRepository profileRepo;

    public ProfileService(ProfileJpaRepository profileRepo) {
        this.profileRepo = profileRepo;
    }

    @Transactional(readOnly = true)
    public ProfileJpaEntity getMe(long userId) {
        return profileRepo.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "프로필을 찾을 수 없습니다."));
    }

    @Transactional
    public void updateMe(long userId, String name, String instrumentRaw, String levelRaw, String regionRaw) {
        ProfileJpaEntity profile = profileRepo.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "프로필을 찾을 수 없습니다."));

        if (name != null) profile.setName(name);
        if (instrumentRaw != null) profile.setInstrument(Instrument.from(instrumentRaw).name());
        if (levelRaw != null) profile.setLevel(Level.from(levelRaw).name());
        if (regionRaw != null) profile.setRegion(Region.from(regionRaw).name());
    }
}
