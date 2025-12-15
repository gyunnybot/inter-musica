package kr.co.inter_musica.auth.dto;

public class SignupResponse {
    private Long userId;
    private String email;

    public SignupResponse() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
