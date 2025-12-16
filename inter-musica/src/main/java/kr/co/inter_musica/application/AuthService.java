package kr.co.inter_musica.application;

import kr.co.inter_musica.domain.enums.Instrument;
import kr.co.inter_musica.domain.enums.Level;
import kr.co.inter_musica.domain.enums.Region;
import kr.co.inter_musica.infrastructure.persistence.entity.ProfileJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.entity.UserJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.jpa.ProfileJpaRepository;
import kr.co.inter_musica.infrastructure.persistence.jpa.UserJpaRepository;
import kr.co.inter_musica.infrastructure.security.jwt.JwtTokenProvider;
import kr.co.inter_musica.presentation.exception.ApiException;
import kr.co.inter_musica.presentation.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserJpaRepository userRepo;
    private final ProfileJpaRepository profileRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserJpaRepository userRepo,
                       ProfileJpaRepository profileRepo,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public void signup(String email, String rawPassword, String name, String instrumentRaw, String levelRaw, String regionRaw) {
        if (userRepo.existsByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_DUPLICATED, "이미 사용 중인 이메일입니다.");
        }

        // 도메인 강제(유효성은 여기서 통과해야 함)
        Instrument instrument = Instrument.from(instrumentRaw);
        Region region = Region.from(regionRaw);

        String encoded = passwordEncoder.encode(rawPassword);

        UserJpaEntity user = new UserJpaEntity(email, encoded);
        userRepo.save(user);

        Level level = Level.from(levelRaw);

        ProfileJpaEntity profile = new ProfileJpaEntity(
                user,
                name,
                instrument.name(), // DB에는 VARCHAR로 저장
                level.name(),
                region.name()
        );
        profileRepo.save(profile);
    }

    @Transactional(readOnly = true)
    public String login(String email, String rawPassword) {
        UserJpaEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
    }
}
