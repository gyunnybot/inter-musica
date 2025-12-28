package kr.co.inter_musica.presentation.dto.joinrequest;

import kr.co.inter_musica.domain.enums.JoinRequestStatus;

import java.time.Instant;
import java.util.List;

public class MyJoinRequestResponse {
    private Long id;
    private JoinRequestStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private String message;
    private TeamSummary team;
    private PositionSummary position;

    private boolean cancellable;

    public MyJoinRequestResponse(
            Long id,
            JoinRequestStatus status,
            Instant createdAt,
            Instant updatedAt,
            String message,
            TeamSummary team,
            PositionSummary position,
            boolean cancellable
    ) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.message = message;
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

    public String getMessage() {
        return message;
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
        private List<String> practiceRegions;

        public TeamSummary(Long id, String teamName, String practiceRegion, List<String> practiceRegions) {
            this.id = id;
            this.teamName = teamName;
            this.practiceRegion = practiceRegion;
            this.practiceRegions = practiceRegions;
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

        public List<String> getPracticeRegions() {
            return practiceRegions;
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
