package kr.co.inter_musica.presentation.dto.joinrequest;

import kr.co.inter_musica.domain.enums.JoinRequestStatus;

import java.time.Instant;

public class MyJoinRequestResponse {
    private Long id;
    private JoinRequestStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private TeamSummary team;
    private PositionSummary position;

    private boolean cancellable;

    public MyJoinRequestResponse(
            Long id,
            JoinRequestStatus status,
            Instant createdAt,
            Instant updatedAt,
            TeamSummary team,
            PositionSummary position,
            boolean cancellable
    ) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.team = team;
        this.position = position;
        this.cancellable = cancellable;
    }

    public Long getId() {
        return id;
    }

    public JoinRequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public TeamSummary getTeam() {
        return team;
    }

    public PositionSummary getPosition() {
        return position;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public static class TeamSummary {
        private Long id;
        private String teamName;
        private String practiceRegion;

        public TeamSummary(Long id, String teamName, String practiceRegion) {
            this.id = id;
            this.teamName = teamName;
            this.practiceRegion = practiceRegion;
        }

        public Long getId() {
            return id;
        }

        public String getTeamName() {
            return teamName;
        }

        public String getPracticeRegion() {
            return practiceRegion;
        }
    }

    public static class PositionSummary {
        private Long id;
        private String instrument;
        private String requiredLevelMin;
        private int capacity;

        public PositionSummary(Long id, String instrument, String requiredLevelMin, int capacity) {
            this.id = id;
            this.instrument = instrument;
            this.requiredLevelMin = requiredLevelMin;
            this.capacity = capacity;
        }

        public Long getId() {
            return id;
        }

        public String getInstrument() {
            return instrument;
        }

        public String getRequiredLevelMin() {
            return requiredLevelMin;
        }

        public int getCapacity() {
            return capacity;
        }
    }
}
