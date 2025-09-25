-- 테이블 전체 삭제 (개발 초기 안전을 위해 사용)
DROP TABLE IF EXISTS `refresh_tokens`;
DROP TABLE IF EXISTS `personal_info`;
DROP TABLE IF EXISTS `manses`;
DROP TABLE IF EXISTS `users`;


-- 1. 사용자 정보 테이블 (users)
CREATE TABLE `users` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT,
                         `username` VARCHAR(255) UNIQUE,
                         `email` VARCHAR(255) UNIQUE,
                         `password` VARCHAR(255),
                         `name` VARCHAR(255),

    -- Role enum ('ADMIN', 'MANAGER', 'USER')
                         `role` ENUM('ADMIN', 'MANAGER', 'USER') DEFAULT 'USER',
                         `enabled` BOOLEAN NOT NULL DEFAULT TRUE,

    -- SocialType enum 및 social_id
                         `social_type` TINYINT,
                         `social_id` VARCHAR(255),

                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 2. 만세력 기준 데이터 테이블 (manses)
CREATE TABLE `manses` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT,

    -- solar_date에 UNIQUE 제약조건 추가 (만세력 데이터의 무결성 핵심)
                          `solar_date` DATE NOT NULL UNIQUE,
                          `lunar_date` DATE NOT NULL,

                          `season` VARCHAR(10) DEFAULT NULL,

                          `season_start_time` TIMESTAMP NULL DEFAULT NULL,

                          `leap_month` BOOLEAN DEFAULT NULL, -- BOOLEAN은 MySQL에서 TINYINT(1)로 처리

                          `year_sky` VARCHAR(10) DEFAULT NULL,
                          `year_ground` VARCHAR(10) DEFAULT NULL,
                          `month_sky` VARCHAR(10) DEFAULT NULL,
                          `month_ground` VARCHAR(10) DEFAULT NULL,
                          `day_sky` VARCHAR(10) DEFAULT NULL,
                          `day_ground` VARCHAR(10) DEFAULT NULL,

                          `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=73443 DEFAULT CHARSET=utf8mb4;


-- 3. 사용자 개인 정보 테이블 (personal_info)
CREATE TABLE `personal_info` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `user_id` BIGINT, -- users 테이블을 참조할 외래 키

                                 `name` VARCHAR(255),
                                 `birth_date` VARCHAR(255),
                                 `birth_time` VARCHAR(255),

    -- CalendarType enum (TINYINT로 매핑 추정)
                                 `calendar_type` TINYINT,

    -- Gender enum ('FEMALE','MALE')
                                 `gender` ENUM('FEMALE', 'MALE'),

                                 `is_time_unknown` BOOLEAN NOT NULL DEFAULT FALSE,
                                 `midnight_adjust` BOOLEAN NOT NULL DEFAULT FALSE,

                                 `location_id` VARCHAR(255),
                                 `city` VARCHAR(255),

                                 `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                 PRIMARY KEY (`id`),
    -- user_id를 users 테이블의 id에 연결
                                 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 4. 리프레시 토큰 테이블 (refresh_tokens)
CREATE TABLE `refresh_tokens` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                                  `token` VARCHAR(500) NOT NULL UNIQUE,
                                  `user_id` BIGINT NOT NULL,
                                  `expires_at` TIMESTAMP NOT NULL,
                                  `revoked` BOOLEAN NOT NULL DEFAULT FALSE,

                                  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `used_at` TIMESTAMP NULL DEFAULT NULL,

                                  PRIMARY KEY (`id`),
    -- user_id를 users 테이블의 id에 연결 (계정 삭제 시 토큰도 삭제)
                                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 5. 인덱스 생성 (검색 성능 최적화)
CREATE INDEX idx_manses_solar_date ON manses(solar_date);
CREATE INDEX idx_manses_lunar_date ON manses(lunar_date);
-- solar_date와 leap_month의 조합은 사용자의 요청대로 유지
CREATE INDEX idx_solar_leap ON manses(solar_date, leap_month);

-- 외래 키가 있는 테이블에는 인덱스 생성
CREATE INDEX idx_personal_info_user_id ON personal_info(user_id);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
