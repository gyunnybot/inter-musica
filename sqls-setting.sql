create database inter_musica;

use inter_musica;

-- (선택) DB 생성
-- CREATE DATABASE inter_musica CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- USE inter_musica;

-- =========================================
-- 1) USERS
-- =========================================
CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(320) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================
-- 2) PROFILES (User 1:1, PK=FK)
-- =========================================
CREATE TABLE IF NOT EXISTS profiles (
  user_id BIGINT NOT NULL,
  instrument VARCHAR(64) NOT NULL,
  level VARCHAR(64) NOT NULL,
  region VARCHAR(64) NOT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (user_id),

  CONSTRAINT fk_profiles_user_id
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,

  CONSTRAINT chk_profiles_level
    CHECK (level IN (
      'HOBBY_UNDER_A_YEAR',
      'HOBBY_UNDER_FIVE_YEAR',
      'HOBBY_OVER_FIVE_YEAR',
      'MAJOR_STUDENT'
    ))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================
-- 3) TEAMS
-- =========================================
CREATE TABLE IF NOT EXISTS teams (
  id BIGINT NOT NULL AUTO_INCREMENT,
  leader_user_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  intro VARCHAR(255) NOT NULL,
  goal VARCHAR(255) NOT NULL,
  practice_info VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  KEY idx_teams_leader_user_id (leader_user_id),

  CONSTRAINT fk_teams_leader_user_id
    FOREIGN KEY (leader_user_id) REFERENCES users(id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================
-- 4) SLOTS
-- =========================================
CREATE TABLE IF NOT EXISTS slots (
  id BIGINT NOT NULL AUTO_INCREMENT,
  team_id BIGINT NOT NULL,
  instrument VARCHAR(64) NOT NULL,
  capacity INT NOT NULL,
  required_level_min VARCHAR(64) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  KEY idx_slots_team_id (team_id),
  KEY idx_slots_instrument (instrument),

  CONSTRAINT fk_slots_team_id
    FOREIGN KEY (team_id) REFERENCES teams(id)
    ON DELETE CASCADE,

  CONSTRAINT chk_slots_capacity
    CHECK (capacity >= 1),

  CONSTRAINT chk_slots_required_level_min
    CHECK (required_level_min IN (
      'HOBBY_UNDER_A_YEAR',
      'HOBBY_UNDER_FIVE_YEAR',
      'HOBBY_OVER_FIVE_YEAR',
      'MAJOR_STUDENT'
    ))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================
-- 5) JOIN_REQUESTS
--   - 같은 slot_id + applicant_user_id 에서 APPLIED 중복 방지
--     (MySQL에서는 부분 유니크가 없으므로 Generated Column로 구현)
-- =========================================
CREATE TABLE IF NOT EXISTS join_requests (
  id BIGINT NOT NULL AUTO_INCREMENT,
  team_id BIGINT NOT NULL,
  slot_id BIGINT NOT NULL,
  applicant_user_id BIGINT NOT NULL,

  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  decided_at DATETIME NULL,
  decided_by_user_id BIGINT NULL,

  -- status='APPLIED'일 때만 1, 아니면 NULL (UNIQUE가 NULL은 중복 허용)
  applied_uniq TINYINT GENERATED ALWAYS AS (
    CASE WHEN status = 'APPLIED' THEN 1 ELSE NULL END
  ) STORED,

  PRIMARY KEY (id),

  KEY idx_join_requests_team_id (team_id),
  KEY idx_join_requests_slot_id (slot_id),
  KEY idx_join_requests_applicant_user_id (applicant_user_id),
  KEY idx_join_requests_status (status),

  -- 같은 슬롯에 "진행중(APPLIED)" 지원은 1개만 허용
  UNIQUE KEY uk_join_requests_applied_once (slot_id, applicant_user_id, applied_uniq),

  CONSTRAINT fk_join_requests_team_id
    FOREIGN KEY (team_id) REFERENCES teams(id)
    ON DELETE CASCADE,

  CONSTRAINT fk_join_requests_slot_id
    FOREIGN KEY (slot_id) REFERENCES slots(id)
    ON DELETE CASCADE,

  CONSTRAINT fk_join_requests_applicant_user_id
    FOREIGN KEY (applicant_user_id) REFERENCES users(id)
    ON DELETE CASCADE,

  CONSTRAINT fk_join_requests_decided_by_user_id
    FOREIGN KEY (decided_by_user_id) REFERENCES users(id)
    ON DELETE SET NULL,

  CONSTRAINT chk_join_requests_status
    CHECK (status IN ('APPLIED', 'ACCEPTED', 'REJECTED', 'CANCELED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================
-- 6) TEAM_MEMBERS
--   - "동시에 한 팀만 소속" 정책: ACTIVE 멤버십을 user_id 기준 1개로 제한
--     (Generated Column + UNIQUE)
-- =========================================
CREATE TABLE IF NOT EXISTS team_members (
  id BIGINT NOT NULL AUTO_INCREMENT,
  team_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  slot_id BIGINT NOT NULL,

  status VARCHAR(16) NOT NULL,
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  left_at DATETIME NULL,

  -- status='ACTIVE'일 때만 1, 아니면 NULL (UNIQUE가 NULL은 중복 허용)
  active_uniq TINYINT GENERATED ALWAYS AS (
    CASE WHEN status = 'ACTIVE' THEN 1 ELSE NULL END
  ) STORED,

  PRIMARY KEY (id),

  KEY idx_team_members_team_id (team_id),
  KEY idx_team_members_user_id (user_id),
  KEY idx_team_members_slot_id (slot_id),
  KEY idx_team_members_status (status),

  -- ACTIVE 멤버십은 user_id 기준 1개만 허용
  UNIQUE KEY uk_team_members_one_active_per_user (user_id, active_uniq),

  CONSTRAINT fk_team_members_team_id
    FOREIGN KEY (team_id) REFERENCES teams(id)
    ON DELETE CASCADE,

  CONSTRAINT fk_team_members_user_id
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,

  CONSTRAINT fk_team_members_slot_id
    FOREIGN KEY (slot_id) REFERENCES slots(id)
    ON DELETE CASCADE,

  CONSTRAINT chk_team_members_status
    CHECK (status IN ('ACTIVE', 'LEFT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

show tables;

select * from join_requests;
select * from profiles;
select * from slots;
select * from team_members;
select * from teams;
select * from users;