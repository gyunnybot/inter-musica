package kr.co.inter_musica.auth.service;

import kr.co.inter_musica.user.dto.ProfileResponse;
import kr.co.inter_musica.user.dto.ProfileUpdateRequest;

public interface ProfileService {
    ProfileResponse getMyProfile(Long userId);
    ProfileResponse updateMyProfile(Long userId, ProfileUpdateRequest request);
}
