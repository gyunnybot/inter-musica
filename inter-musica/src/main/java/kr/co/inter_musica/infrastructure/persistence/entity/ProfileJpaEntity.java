package kr.co.inter_musica.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "profiles")
public class ProfileJpaEntity {

    @Id
    @Column(name = "profile_id")
    private Long profileId; // == users.id

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id")
    private UserJpaEntity user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 30)
    private String instrument; // "VOCAL" ...

    @Column(nullable = false, length = 30)
    private String level;

    @Column(nullable = false, length = 30)
    private String region; // "SEOUL" ...

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProfileJpaEntity() {
    }

    public ProfileJpaEntity(UserJpaEntity user, String name, String instrument, String level, String region) {
        this.user = user;
        this.name = name;
        this.instrument = instrument;
        this.level = level;
        this.region = region;
    }

    @PrePersist
    void prePersist() {
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getProfileId() { return profileId; }
    public UserJpaEntity getUser() { return user; }
    public String getName() { return name; }
    public String getInstrument() { return instrument; }
    public String getLevel() { return level; }
    public String getRegion() { return region; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setName(String name) { this.name = name; }
    public void setInstrument(String instrument) { this.instrument = instrument; }
    public void setLevel(String level) { this.level = level; }
    public void setRegion(String region) { this.region = region; }
}
