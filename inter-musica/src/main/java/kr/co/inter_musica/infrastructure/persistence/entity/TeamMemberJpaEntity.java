package kr.co.inter_musica.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

// @UniqueConstraint : 같은 팀에 같은 유저가 중복되는 상황 방지
@Entity
@Table(name = "team_members",
        uniqueConstraints = @UniqueConstraint(name="uk_team_members_team_user", columnNames = {"team_id","user_id"}))
public class TeamMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="team_id", nullable = false)
    private Long teamId;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    protected TeamMemberJpaEntity() {}

    public TeamMemberJpaEntity(Long teamId, Long userId) {
        this.teamId = teamId;
        this.userId = userId;
    }

    @PrePersist
    void prePersist() {
        this.joinedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getTeamId() { return teamId; }
    public Long getUserId() { return userId; }
    public Instant getJoinedAt() { return joinedAt; }
}
