package kr.co.inter_musica.user.domain;

import jakarta.persistence.*;
import kr.co.inter_musica.team.domain.enumm.Level;

import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @Column(name = "user_id")
    private Long userId; // PK=FK

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_profiles_user_id"))
    private User user;

    @Column(nullable = false)
    private String instrument;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private Level level;

    @Column(nullable = false)
    private String region;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Profile() { }

    @PrePersist
    @PreUpdate
    protected void touchUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
