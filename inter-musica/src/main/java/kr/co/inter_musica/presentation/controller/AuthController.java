package kr.co.inter_musica.presentation.controller;

import jakarta.validation.Valid;
import kr.co.inter_musica.application.AuthService;
import kr.co.inter_musica.presentation.dto.auth.LoginRequest;
import kr.co.inter_musica.presentation.dto.auth.LoginResponse;
import kr.co.inter_musica.presentation.dto.auth.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody SignupRequest signupRequest
    ) {
        authService.signup(
                signupRequest.getEmail(),
                signupRequest.getPassword(),
                signupRequest.getName(),
                signupRequest.getInstrument(),
                signupRequest.getLevel(),
                signupRequest.getRegion()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

        return ResponseEntity.ok(new LoginResponse(token));
    }
}
