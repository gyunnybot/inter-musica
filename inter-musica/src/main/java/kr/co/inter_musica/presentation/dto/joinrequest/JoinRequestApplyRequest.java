package kr.co.inter_musica.presentation.dto.joinrequest;

import jakarta.validation.constraints.Size;

public class JoinRequestApplyRequest {
    @Size(max = 500)
    private String message;

    public JoinRequestApplyRequest() {}

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }
}
