package kr.co.inter_musica.auth.controller;

import kr.co.inter_musica.auth.dto.LoginRequest;
import kr.co.inter_musica.auth.dto.LoginResponse;
import kr.co.inter_musica.auth.dto.SignupRequest;
import kr.co.inter_musica.auth.dto.SignupResponse;
import kr.co.inter_musica.auth.service.AuthService;
import kr.co.inter_musica.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@RequestBody SignupRequest request) {
        SignupResponse res = authService.signup(request);
        return ResponseEntity.status(201).body(new ApiResponse<SignupResponse>(res));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse res = authService.login(request);
        return new ApiResponse<LoginResponse>(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestAttribute("currentUserId") Long currentUserId) {
        authService.logout(currentUserId);
        return ResponseEntity.noContent().build();
    }
}
