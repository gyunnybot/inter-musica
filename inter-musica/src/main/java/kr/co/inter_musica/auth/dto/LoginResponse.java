package kr.co.inter_musica.auth.dto;

public class LoginResponse {
    private String accessToken;
    private Long userId;

    public LoginResponse() {}

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
