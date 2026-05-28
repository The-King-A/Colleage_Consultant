-- 志愿方案表（已有库可单独执行）
USE volunteer_assistant;

CREATE TABLE IF NOT EXISTS `volunteer_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `school_province` VARCHAR(20) DEFAULT NULL,
  `score_province` VARCHAR(20) DEFAULT NULL,
  `subject_type` VARCHAR(20) DEFAULT NULL,
  `user_score` INT DEFAULT NULL,
  `plan_json` MEDIUMTEXT DEFAULT NULL,
  `note` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
