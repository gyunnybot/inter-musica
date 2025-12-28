package kr.co.inter_musica.presentation.dto.teamchat;

import java.time.Instant;

public class TeamChatMessageResponse {

    private Long id;
    private Long teamId;
    private Long senderUserId;
    private String senderName;
    private String message;
    private Instant createdAt;

    public TeamChatMessageResponse() {
    }

    public TeamChatMessageResponse(Long id, Long teamId, Long senderUserId, String senderName, String message, Instant createdAt) {
        this.id = id;
        this.teamId = teamId;
        this.senderUserId = senderUserId;
        this.senderName = senderName;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getTeamId() { return teamId; }
    public Long getSenderUserId() { return senderUserId; }
    public String getSenderName() { return senderName; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public void setSenderUserId(Long senderUserId) { this.senderUserId = senderUserId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setMessage(String message) { this.message = message; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
