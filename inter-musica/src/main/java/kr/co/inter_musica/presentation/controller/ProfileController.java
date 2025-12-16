package kr.co.inter_musica.presentation.controller;

import jakarta.validation.Valid;
import kr.co.inter_musica.application.ProfileService;
import kr.co.inter_musica.infrastructure.persistence.entity.ProfileJpaEntity;
import kr.co.inter_musica.presentation.security.SecurityUtil;
import kr.co.inter_musica.presentation.dto.profile.ProfileResponse;
import kr.co.inter_musica.presentation.dto.profile.ProfileUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> me() {
        long userId = SecurityUtil.currentUserId();
        ProfileJpaEntity profile = profileService.getMe(userId);

        ProfileResponse res = new ProfileResponse(
                profile.getProfileId(),
                profile.getName(),
                profile.getInstrument(),
                profile.getLevel(),
                profile.getRegion(),
                profile.getUpdatedAt()
        );
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> updateMe(@Valid @RequestBody ProfileUpdateRequest req) {
        long userId = SecurityUtil.currentUserId();
        profileService.updateMe(userId, req.getName(), req.getInstrument(), req.getLevel(), req.getRegion());
        return ResponseEntity.ok().build();
    }
}
