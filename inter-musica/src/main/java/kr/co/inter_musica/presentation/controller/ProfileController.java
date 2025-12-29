package kr.co.inter_musica.presentation.controller;

import jakarta.validation.Valid;
import kr.co.inter_musica.application.ProfileService;
import kr.co.inter_musica.domain.enums.Region;
import kr.co.inter_musica.infrastructure.persistence.entity.ProfileJpaEntity;
import kr.co.inter_musica.domain.security.SecurityUtil;
import kr.co.inter_musica.presentation.dto.profile.ProfileResponse;
import kr.co.inter_musica.presentation.dto.profile.ProfileUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getProfile() {
        long userId = SecurityUtil.currentUserId();

        ProfileJpaEntity profile = profileService.getProfile(userId);

        ProfileResponse profileResponse = new ProfileResponse(
                profile.getProfileId(),
                profile.getName(),
                profile.getInstrument(),
                profile.getLevel(),
                Region.parseStored(profile.getRegion()),
                profile.getUpdatedAt()
        );

        return ResponseEntity.ok(profileResponse);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfileByUserId(
            @PathVariable("userId") long userId
    ) {
        ProfileJpaEntity profile = profileService.getProfile(userId);

        ProfileResponse profileResponse = new ProfileResponse(
                profile.getProfileId(),
                profile.getName(),
                profile.getInstrument(),
                profile.getLevel(),
                Region.parseStored(profile.getRegion()),
                profile.getUpdatedAt()
        );

        return ResponseEntity.ok(profileResponse);
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest profileUpdateRequest
    ) {
        long userId = SecurityUtil.currentUserId();

        profileService.updateProfile(
                userId,
                profileUpdateRequest.getName(),
                profileUpdateRequest.getInstrument(),
                profileUpdateRequest.getLevel(),
                profileUpdateRequest.getPracticeRegions()
        );

        return ResponseEntity.ok().build();
    }
}
