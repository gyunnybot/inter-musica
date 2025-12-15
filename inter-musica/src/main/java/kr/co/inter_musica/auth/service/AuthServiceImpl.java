package kr.co.inter_musica.auth.service;

import jakarta.transaction.Transactional;
import kr.co.inter_musica.auth.dto.*;
import kr.co.inter_musica.auth.jwt.JwtProvider;
import kr.co.inter_musica.exception.AppException;
import kr.co.inter_musica.exception.ErrorCode;
import kr.co.inter_musica.user.domain.Profile;
import kr.co.inter_musica.user.domain.User;
import kr.co.inter_musica.user.repository.ProfileRepository;
import kr.co.inter_musica.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthServiceImpl(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtProvider jwtProvider
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        ProfileCreateRequest p = request.getProfile();
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setInstrument(p.getInstrument());
        profile.setLevel(p.getLevel());
        profile.setRegion(p.getRegion());
        // MapsId 때문에 userId는 user를 통해 자동으로 매핑됨
        profileRepository.save(profile);

        SignupResponse res = new SignupResponse();
        res.setUserId(user.getId());
        res.setEmail(user.getEmail());
        return res;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        String token = jwtProvider.generateAccessToken(user.getId());

        LoginResponse res = new LoginResponse();
        res.setAccessToken(token);
        res.setUserId(user.getId());
        return res;
    }

    @Override
    public void logout(Long currentUserId) {
        // JWT는 서버 상태가 없어서 MVP에선 기능을 수행하지 않음
    }
}
