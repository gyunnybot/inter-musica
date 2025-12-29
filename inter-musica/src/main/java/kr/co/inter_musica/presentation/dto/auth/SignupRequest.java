package kr.co.inter_musica.presentation.dto.auth;

import jakarta.validation.constraints.*;

import java.util.List;

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

    @NotEmpty
    private List<String> practiceRegions;

    public SignupRequest() {}

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getInstrument() { return instrument; }
    public String getLevel() { return level; }
    public List<String> getPracticeRegions() { return practiceRegions; }

    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setInstrument(String instrument) { this.instrument = instrument; }
    public void setLevel(String level) { this.level = level; }
    public void setPracticeRegions(List<String> practiceRegions) { this.practiceRegions = practiceRegions; }
}
