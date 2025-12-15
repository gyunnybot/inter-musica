package kr.co.inter_musica.auth.service;

import jakarta.transaction.Transactional;
import kr.co.inter_musica.exception.AppException;
import kr.co.inter_musica.exception.ErrorCode;
import kr.co.inter_musica.user.domain.Profile;
import kr.co.inter_musica.user.dto.ProfileResponse;
import kr.co.inter_musica.user.dto.ProfileUpdateRequest;
import kr.co.inter_musica.user.repository.ProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public ProfileResponse getMyProfile(Long userId) {
        Profile p = profileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        return toResponse(p);
    }

    @Override
    @Transactional
    public ProfileResponse updateMyProfile(Long userId, ProfileUpdateRequest request) {
        Profile p = profileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        p.setInstrument(request.getInstrument());
        p.setLevel(request.getLevel());
        p.setRegion(request.getRegion());

        return toResponse(p);
    }

    private ProfileResponse toResponse(Profile p) {
        ProfileResponse res = new ProfileResponse();
        res.setUserId(p.getUserId());
        res.setInstrument(p.getInstrument());
        res.setLevel(p.getLevel());
        res.setRegion(p.getRegion());
        return res;
    }
}
