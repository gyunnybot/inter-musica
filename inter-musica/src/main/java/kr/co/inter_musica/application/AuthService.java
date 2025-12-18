package kr.co.inter_musica.application;

import kr.co.inter_musica.domain.enums.Instrument;
import kr.co.inter_musica.domain.enums.Level;
import kr.co.inter_musica.domain.enums.Region;
import kr.co.inter_musica.infrastructure.persistence.entity.ProfileJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.entity.UserJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.jpa.ProfileJpaRepository;
import kr.co.inter_musica.infrastructure.persistence.jpa.UserJpaRepository;
import kr.co.inter_musica.infrastructure.security.jwt.JwtTokenProvider;
import kr.co.inter_musica.domain.exception.ApiException;
import kr.co.inter_musica.domain.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserJpaRepository userJpaRepository;
    private final ProfileJpaRepository profileJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthService(UserJpaRepository userJpaRepository,
                       ProfileJpaRepository profileJpaRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {

        this.userJpaRepository = userJpaRepository;
        this.profileJpaRepository = profileJpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;

    }

    @Transactional
    public void signup(String email, String rawPassword, String name, String instrumentRaw, String levelRaw, String regionRaw) {
        if (userJpaRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_DUPLICATED, "이미 사용 중인 이메일입니다.");
        }

        // 도메인 강제 (유효성 검사는 여기서 통과해야 함)
        Instrument instrument = Instrument.from(instrumentRaw);
        Region region = Region.from(regionRaw);

        String encoded = passwordEncoder.encode(rawPassword);

        UserJpaEntity user = new UserJpaEntity(email, encoded);
        userJpaRepository.save(user);

        Level level = Level.from(levelRaw);

        ProfileJpaEntity profile = new ProfileJpaEntity(
                user,
                name,
                instrument.name(),
                level.name(),
                region.name()
        );
        profileJpaRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public String login(String email, String rawPassword) {
        UserJpaEntity user = userJpaRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
    }
}
