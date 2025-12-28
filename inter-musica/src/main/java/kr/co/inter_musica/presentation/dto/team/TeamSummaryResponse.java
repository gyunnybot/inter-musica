package kr.co.inter_musica.presentation.dto.team;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

public class TeamSummaryResponse {
    private Long id;
    private String teamName;
    private String practiceRegion;
    private List<String> practiceRegions;
    private String practiceNote;
    private LocalTime coreTimeStart;
    private LocalTime coreTimeEnd;
    private Long leaderUserId;
    private Instant createdAt;

    public TeamSummaryResponse() {}

    public TeamSummaryResponse(Long id, String teamName, String practiceRegion, List<String> practiceRegions, String practiceNote,
                               LocalTime coreTimeStart, LocalTime coreTimeEnd,
                               Long leaderUserId, Instant createdAt) {
        this.id = id;
        this.teamName = teamName;
        this.practiceRegion = practiceRegion;
        this.practiceRegions = practiceRegions;
        this.practiceNote = practiceNote;
        this.coreTimeStart = coreTimeStart;
        this.coreTimeEnd = coreTimeEnd;
        this.leaderUserId = leaderUserId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTeamName() { return teamName; }
    public String getPracticeRegion() { return practiceRegion; }
    public List<String> getPracticeRegions() { return practiceRegions; }
    public String getPracticeNote() { return practiceNote; }
    public LocalTime getCoreTimeStart() { return coreTimeStart; }
    public LocalTime getCoreTimeEnd() { return coreTimeEnd; }
    public Long getLeaderUserId() { return leaderUserId; }
    public Instant getCreatedAt() { return createdAt; }
}
