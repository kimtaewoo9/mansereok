DROP TABLE IF EXISTS `manses`;

CREATE TABLE `manses` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT,

                          `solar_date` DATE NOT NULL,
                          `lunar_date` DATE NOT NULL,

                          `season` VARCHAR(10) DEFAULT NULL,

                          `season_start_time` TIMESTAMP NULL DEFAULT NULL,

                          `leap_month` BOOLEAN DEFAULT NULL,

                          `year_sky` VARCHAR(10) DEFAULT NULL,
                          `year_ground` VARCHAR(10) DEFAULT NULL,
                          `month_sky` VARCHAR(10) DEFAULT NULL,
                          `month_ground` VARCHAR(10) DEFAULT NULL,
                          `day_sky` VARCHAR(10) DEFAULT NULL,
                          `day_ground` VARCHAR(10) DEFAULT NULL,

                          `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=73443
  DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_solar_date ON manses(solar_date);
CREATE INDEX idx_lunar_date ON manses(lunar_date);
CREATE INDEX idx_solar_leap ON manses(solar_date, leap_month);
