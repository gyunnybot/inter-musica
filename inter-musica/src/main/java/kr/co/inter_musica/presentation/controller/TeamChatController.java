package kr.co.inter_musica.presentation.controller;

import jakarta.validation.Valid;
import kr.co.inter_musica.application.TeamChatService;
import kr.co.inter_musica.domain.security.SecurityUtil;
import kr.co.inter_musica.presentation.dto.teamchat.TeamChatMessageRequest;
import kr.co.inter_musica.presentation.dto.teamchat.TeamChatMessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams/{teamId}/chat")
public class TeamChatController {

    private final TeamChatService teamChatService;

    public TeamChatController(TeamChatService teamChatService) {
        this.teamChatService = teamChatService;
    }

    @GetMapping
    public ResponseEntity<List<TeamChatMessageResponse>> getMessages(
            @PathVariable Long teamId
    ) {
        long userId = SecurityUtil.currentUserId();
        List<TeamChatMessageResponse> list = teamChatService.getMessages(userId, teamId);
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<TeamChatMessageResponse> sendMessage(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamChatMessageRequest request
    ) {
        long userId = SecurityUtil.currentUserId();
        TeamChatMessageResponse response = teamChatService.sendMessage(userId, teamId, request.getMessage());
        return ResponseEntity.ok(response);
    }
}
