package kr.co.inter_musica.auth.service;

import kr.co.inter_musica.auth.dto.LoginRequest;
import kr.co.inter_musica.auth.dto.LoginResponse;
import kr.co.inter_musica.auth.dto.SignupRequest;
import kr.co.inter_musica.auth.dto.SignupResponse;

public interface AuthService {
    SignupResponse signup(SignupRequest request);
    LoginResponse login(LoginRequest request);
    void logout(Long currentUserId);
}