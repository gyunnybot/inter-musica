package kr.co.inter_musica.presentation.dto.auth;

import jakarta.validation.constraints.*;

public class SignupRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String instrument;

    @NotBlank
    private String level;

    @NotBlank
    private String region;

    public SignupRequest() {}

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getInstrument() { return instrument; }
    public String getLevel() { return level; }
    public String getRegion() { return region; }

    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setInstrument(String instrument) { this.instrument = instrument; }
    public void setLevel(String level) { this.level = level; }
    public void setRegion(String region) { this.region = region; }
}
