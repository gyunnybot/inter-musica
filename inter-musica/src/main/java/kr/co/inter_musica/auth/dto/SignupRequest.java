package kr.co.inter_musica.auth.dto;

public class SignupRequest {
    private String email;
    private String password;
    private ProfileCreateRequest profile;

    public SignupRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public ProfileCreateRequest getProfile() { return profile; }
    public void setProfile(ProfileCreateRequest profile) { this.profile = profile; }
}
