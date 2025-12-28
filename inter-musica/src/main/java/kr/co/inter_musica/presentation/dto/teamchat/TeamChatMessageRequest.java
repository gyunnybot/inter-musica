package kr.co.inter_musica.presentation.dto.teamchat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TeamChatMessageRequest {

    @NotBlank(message = "메시지를 입력해 주세요.")
    @Size(max = 500, message = "메시지는 500자 이하로 입력해 주세요.")
    private String message;

    public TeamChatMessageRequest() {
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
