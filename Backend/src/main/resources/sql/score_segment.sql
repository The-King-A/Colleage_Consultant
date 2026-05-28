-- 一分一段表 + 填报向导进度表（已有库可单独执行）
USE volunteer_assistant;

CREATE TABLE IF NOT EXISTS `score_segment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `year` INT NOT NULL,
  `province` VARCHAR(50) NOT NULL,
  `subject_type` VARCHAR(20) NOT NULL,
  `score` INT NOT NULL,
  `segment_count` INT DEFAULT NULL,
  `cumulative_rank` INT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seg` (`year`, `province`, `subject_type`, `score`),
  KEY `idx_prov_year` (`province`, `year`, `subject_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `volunteer_wizard_progress` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `current_step` VARCHAR(32) NOT NULL DEFAULT 'ENTER_SCORE',
  `completed_steps` VARCHAR(500) DEFAULT NULL,
  `user_score` INT DEFAULT NULL,
  `score_province` VARCHAR(20) DEFAULT NULL,
  `subject_type` VARCHAR(20) DEFAULT NULL,
  `school_province` VARCHAR(20) DEFAULT NULL,
  `holland_code` VARCHAR(10) DEFAULT NULL,
  `last_plan_id` BIGINT DEFAULT NULL,
  `meta_json` TEXT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
