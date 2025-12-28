package kr.co.inter_musica.application;

import kr.co.inter_musica.domain.enums.ErrorCode;
import kr.co.inter_musica.domain.exception.ApiException;
import kr.co.inter_musica.infrastructure.persistence.entity.ProfileJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.entity.TeamChatMessageJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.entity.TeamJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.jpa.ProfileJpaRepository;
import kr.co.inter_musica.infrastructure.persistence.jpa.TeamChatMessageJpaRepository;
import kr.co.inter_musica.infrastructure.persistence.jpa.TeamJpaRepository;
import kr.co.inter_musica.infrastructure.persistence.jpa.TeamMemberJpaRepository;
import kr.co.inter_musica.presentation.dto.teamchat.TeamChatMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeamChatService {

    private final TeamJpaRepository teamJpaRepository;
    private final TeamMemberJpaRepository teamMemberJpaRepository;
    private final TeamChatMessageJpaRepository teamChatMessageJpaRepository;
    private final ProfileJpaRepository profileJpaRepository;

    @Autowired
    public TeamChatService(TeamJpaRepository teamJpaRepository,
                           TeamMemberJpaRepository teamMemberJpaRepository,
                           TeamChatMessageJpaRepository teamChatMessageJpaRepository,
                           ProfileJpaRepository profileJpaRepository) {
        this.teamJpaRepository = teamJpaRepository;
        this.teamMemberJpaRepository = teamMemberJpaRepository;
        this.teamChatMessageJpaRepository = teamChatMessageJpaRepository;
        this.profileJpaRepository = profileJpaRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamChatMessageResponse> getMessages(long currentUserId, Long teamId) {
        TeamJpaEntity team = getTeam(teamId);
        ensureTeamMember(currentUserId, team);

        List<TeamChatMessageJpaEntity> messages = teamChatMessageJpaRepository
                .findTop50ByTeamIdOrderByCreatedAtDesc(teamId);
        Collections.reverse(messages);

        Map<Long, String> nameMap = resolveNames(messages);

        return messages.stream()
                .map(m -> new TeamChatMessageResponse(
                        m.getId(),
                        m.getTeamId(),
                        m.getUserId(),
                        nameMap.getOrDefault(m.getUserId(), "알 수 없음"),
                        m.getMessage(),
                        m.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public TeamChatMessageResponse sendMessage(long currentUserId, Long teamId, String message) {
        TeamJpaEntity team = getTeam(teamId);
        ensureTeamMember(currentUserId, team);

        TeamChatMessageJpaEntity saved = teamChatMessageJpaRepository.save(
                new TeamChatMessageJpaEntity(teamId, currentUserId, message)
        );

        String senderName = profileJpaRepository.findById(currentUserId)
                .map(ProfileJpaEntity::getName)
                .orElse("알 수 없음");

        return new TeamChatMessageResponse(
                saved.getId(),
                saved.getTeamId(),
                saved.getUserId(),
                senderName,
                saved.getMessage(),
                saved.getCreatedAt()
        );
    }

    private TeamJpaEntity getTeam(Long teamId) {
        return teamJpaRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(ErrorCode.TEAM_NOT_FOUND, "팀을 찾을 수 없습니다."));
    }

    private void ensureTeamMember(long currentUserId, TeamJpaEntity team) {
        if (team.getLeaderUserId().equals(currentUserId)) {
            return;
        }

        if (!teamMemberJpaRepository.existsByTeamIdAndUserId(team.getId(), currentUserId)) {
            throw new ApiException(ErrorCode.TEAM_FORBIDDEN, "팀 멤버만 이용할 수 있습니다.");
        }
    }

    private Map<Long, String> resolveNames(List<TeamChatMessageJpaEntity> messages) {
        List<Long> userIds = messages.stream()
                .map(TeamChatMessageJpaEntity::getUserId)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ProfileJpaEntity> profiles = profileJpaRepository.findAllById(userIds);
        Map<Long, String> nameMap = new HashMap<>();
        for (ProfileJpaEntity profile : profiles) {
            nameMap.put(profile.getProfileId(), profile.getName());
        }
        return nameMap;
    }
}
