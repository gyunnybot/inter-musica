package kr.co.inter_musica.application;

import kr.co.inter_musica.domain.enums.Instrument;
import kr.co.inter_musica.domain.enums.Level;
import kr.co.inter_musica.domain.enums.Region;
import kr.co.inter_musica.infrastructure.persistence.entity.ProfileJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.jpa.ProfileJpaRepository;
import kr.co.inter_musica.domain.exception.ApiException;
import kr.co.inter_musica.domain.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final ProfileJpaRepository profileJpaRepository;

    @Autowired
    public ProfileService(ProfileJpaRepository profileJpaRepository) {
        this.profileJpaRepository = profileJpaRepository;
    }

    @Transactional(readOnly = true)
    public ProfileJpaEntity getProfile(long userId) {
        return profileJpaRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "프로필을 찾을 수 없습니다."));
    }

    @Transactional
    public void updateProfile(long userId, String name, String instrumentRaw, String levelRaw, String regionRaw) {
        ProfileJpaEntity profile = profileJpaRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "프로필을 찾을 수 없습니다."));

        if (name != null) {
            profile.setName(name);
        }

        if (instrumentRaw != null) {
            profile.setInstrument(Instrument.from(instrumentRaw).name());
        }

        if (levelRaw != null) {
            profile.setLevel(Level.from(levelRaw).name());
        }

        if (regionRaw != null) {
            profile.setRegion(Region.from(regionRaw).name());
        }
    }
}
