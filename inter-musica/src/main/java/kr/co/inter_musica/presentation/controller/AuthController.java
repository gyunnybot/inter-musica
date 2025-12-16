package kr.co.inter_musica.presentation.controller;

import jakarta.validation.Valid;
import kr.co.inter_musica.application.AuthService;
import kr.co.inter_musica.presentation.dto.auth.LoginRequest;
import kr.co.inter_musica.presentation.dto.auth.LoginResponse;
import kr.co.inter_musica.presentation.dto.auth.SignupRequest;
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
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest req) {
        authService.signup(
                req.getEmail(),
                req.getPassword(),
                req.getName(),
                req.getInstrument(),
                req.getLevel(),
                req.getRegion()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = authService.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
