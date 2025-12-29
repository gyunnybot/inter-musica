drop database inter_musica;

create database inter_musica;

use inter_musica;

SET NAMES utf8mb4;
SET time_zone = '+09:00';

-- (선택) 깨끗한 재생성용
-- DROP TABLE 순서는 FK 역순
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS join_requests;
DROP TABLE IF EXISTS team_chat_messages;
DROP TABLE IF EXISTS team_members;
DROP TABLE IF EXISTS position_slots;
DROP TABLE IF EXISTS team_regions;
DROP TABLE IF EXISTS teams;
DROP TABLE IF EXISTS regions;
DROP TABLE IF EXISTS profiles;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- 1) users
-- =========================
CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(190) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 2) profiles (PK=FK to users.id)
--  - code: ProfileJpaEntity @MapsId + @OneToOne join on profile_id
-- =========================
CREATE TABLE profiles (
  profile_id BIGINT NOT NULL,               -- == users.id (AUTO_INCREMENT 금지)
  name VARCHAR(50) NOT NULL,
  instrument VARCHAR(30) NOT NULL,           -- DB는 VARCHAR (A안)
  level VARCHAR(30) NOT NULL,
  region VARCHAR(255) NOT NULL,              -- 연습 가능 지역(복수, CSV)             -- DB는 VARCHAR (A안)
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (profile_id),
  KEY idx_profiles_instrument (instrument),
  CONSTRAINT fk_profiles_user
    FOREIGN KEY (profile_id) REFERENCES users(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 3) teams
--  - code: TeamJpaEntity (leader_user_id is Long, no FK entity mapping)
-- =========================
CREATE TABLE teams (
  id BIGINT NOT NULL AUTO_INCREMENT,
  leader_user_id BIGINT NOT NULL,
  team_name VARCHAR(80) NOT NULL,
  practice_region VARCHAR(30) NOT NULL,      -- DB는 VARCHAR (A안)
  practice_note TEXT NULL,
  core_time_start TIME NULL,
  core_time_end TIME NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_teams_leader_user_id (leader_user_id),
  KEY idx_teams_practice_region (practice_region),
  CONSTRAINT fk_teams_leader_user
    FOREIGN KEY (leader_user_id) REFERENCES users(id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- =========================
-- 3-1) regions
-- =========================
CREATE TABLE regions (
  region_code VARCHAR(30) NOT NULL,
  PRIMARY KEY (region_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 3-2) team_regions
-- =========================
CREATE TABLE team_regions (
  team_id BIGINT NOT NULL,
  region_code VARCHAR(30) NOT NULL,
  PRIMARY KEY (team_id, region_code),
  KEY idx_team_regions_region_code (region_code),
  CONSTRAINT fk_team_regions_team
    FOREIGN KEY (team_id) REFERENCES teams(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_team_regions_region
    FOREIGN KEY (region_code) REFERENCES regions(region_code)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 4) position_slots
--  - 중요: (team_id, id)에 UNIQUE가 있어야 join_requests 복합 FK가 생성됨
-- =========================
CREATE TABLE position_slots (
  id BIGINT NOT NULL AUTO_INCREMENT,
  team_id BIGINT NOT NULL,
  instrument VARCHAR(30) NOT NULL,           -- DB는 VARCHAR (A안)
  capacity INT NOT NULL,
  required_level_min VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_position_slots_team_id (team_id),
  KEY idx_position_slots_team_instrument (team_id, instrument),

  -- 복합 FK 타겟을 위해 필수 (MySQL 규칙)
  UNIQUE KEY uk_position_slots_team_id_id (team_id, id),

  CONSTRAINT fk_position_slots_team
    FOREIGN KEY (team_id) REFERENCES teams(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 5) team_members
--  - code: TeamMemberJpaEntity with unique(team_id, user_id)
-- =========================
CREATE TABLE team_members (
  id BIGINT NOT NULL AUTO_INCREMENT,
  team_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),

  UNIQUE KEY uk_team_members_team_user (team_id, user_id),
  KEY idx_team_members_user_id (user_id),

  CONSTRAINT fk_team_members_team
    FOREIGN KEY (team_id) REFERENCES teams(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_team_members_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 6) join_requests
--  - code: JoinRequestJpaEntity
--    columns: team_id, position_slot_id, applicant_user_id, status(enum), created_at, updated_at
--  - 핵심:
--    (team_id, position_slot_id) -> position_slots(team_id, id) 복합 FK
--    이걸 위해 position_slots에 UNIQUE(team_id, id)가 있어야 함
-- =========================
CREATE TABLE join_requests (
  id BIGINT NOT NULL AUTO_INCREMENT,

  team_id BIGINT NOT NULL,
  position_slot_id BIGINT NOT NULL,
  applicant_user_id BIGINT NOT NULL,

  message VARCHAR(500),

  status ENUM('APPLIED','ACCEPTED','REJECTED','CANCELED') NOT NULL,

  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  -- 서비스/리포지토리 쿼리 최적화용 인덱스들
  KEY idx_join_requests_team_position (team_id, position_slot_id),
  KEY idx_join_requests_position_status (position_slot_id, status),
  KEY idx_join_requests_applicant_status (applicant_user_id, status),
  KEY idx_join_requests_team_position_applicant_status (team_id, position_slot_id, applicant_user_id, status),

  -- 1) 팀 삭제 시 join_requests도 함께 삭제되도록 (CASCADE)
  CONSTRAINT fk_join_requests_team
    FOREIGN KEY (team_id) REFERENCES teams(id)
    ON DELETE CASCADE,

  -- 2) 지원자(user) 삭제 시 join_requests도 함께 삭제 (CASCADE)
  CONSTRAINT fk_join_requests_applicant_user
    FOREIGN KEY (applicant_user_id) REFERENCES users(id)
    ON DELETE CASCADE,

  -- 3) team_id와 position_slot_id의 "소속 일치"를 DB가 보장하는 복합 FK
  --    (team_id, position_slot_id) 가 position_slots(team_id, id)에 존재해야만 insert 가능
  CONSTRAINT fk_join_requests_team_position_slot
    FOREIGN KEY (team_id, position_slot_id)
    REFERENCES position_slots(team_id, id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 7) team_chat_messages
-- =========================
CREATE TABLE team_chat_messages (
  id BIGINT NOT NULL AUTO_INCREMENT,
  team_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  message VARCHAR(500) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_team_chat_messages_team_id (team_id),
  KEY idx_team_chat_messages_user_id (user_id),
  KEY idx_team_chat_messages_team_created_at (team_id, created_at),
  CONSTRAINT fk_team_chat_messages_team
    FOREIGN KEY (team_id) REFERENCES teams(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_team_chat_messages_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

show tables;

select * from join_requests;
select * from position_slots;
select * from profiles;
select * from team_members;
select * from teams;
select * from users;