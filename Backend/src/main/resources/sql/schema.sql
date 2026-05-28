-- Safe schema — uses IF NOT EXISTS, no DROP
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(50) DEFAULT NULL,
  `avatar` VARCHAR(500) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `gender` TINYINT DEFAULT 0,
  `province` VARCHAR(50) DEFAULT NULL,
  `city` VARCHAR(50) DEFAULT NULL,
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
  `status` TINYINT NOT NULL DEFAULT 1,
  `last_login_at` DATETIME DEFAULT NULL,
  `last_login_ip` VARCHAR(50) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `chat_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `session_key` VARCHAR(100) NOT NULL,
  `title` VARCHAR(200) DEFAULT '新对话',
  `message_count` INT NOT NULL DEFAULT 0,
  `last_message` VARCHAR(500) DEFAULT NULL,
  `is_pinned` TINYINT NOT NULL DEFAULT 0,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_key` (`session_key`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `role` VARCHAR(20) NOT NULL,
  `content` TEXT NOT NULL,
  `token_count` INT DEFAULT NULL,
  `search_enabled` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `recommend_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `name` VARCHAR(50) DEFAULT NULL,
  `gender` TINYINT DEFAULT NULL,
  `province` VARCHAR(50) DEFAULT NULL,
  `score` INT NOT NULL,
  `subject_type` VARCHAR(20) NOT NULL,
  `interested_majors` VARCHAR(500) DEFAULT NULL,
  `preferred_regions` VARCHAR(500) DEFAULT NULL,
  `other_requirements` TEXT DEFAULT NULL,
  `ai_result` MEDIUMTEXT DEFAULT NULL,
  `rush_schools` JSON DEFAULT NULL,
  `stable_schools` JSON DEFAULT NULL,
  `safe_schools` JSON DEFAULT NULL,
  `is_favorite` TINYINT NOT NULL DEFAULT 0,
  `share_code` VARCHAR(50) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `favorite_school` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `school_code` VARCHAR(20) NOT NULL,
  `school_name` VARCHAR(100) NOT NULL,
  `school_type` VARCHAR(50) DEFAULT NULL,
  `location` VARCHAR(100) DEFAULT NULL,
  `logo_url` VARCHAR(500) DEFAULT NULL,
  `note` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_school` (`user_id`, `school_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `favorite_major` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `major_code` VARCHAR(20) NOT NULL,
  `major_name` VARCHAR(100) NOT NULL,
  `major_category` VARCHAR(50) DEFAULT NULL,
  `degree_type` VARCHAR(20) DEFAULT NULL,
  `note` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_major` (`user_id`, `major_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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

CREATE TABLE IF NOT EXISTS `interest_test_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `memory_id` VARCHAR(100) NOT NULL,
  `holland_r` INT DEFAULT NULL,
  `holland_i` INT DEFAULT NULL,
  `holland_a` INT DEFAULT NULL,
  `holland_s` INT DEFAULT NULL,
  `holland_e` INT DEFAULT NULL,
  `holland_c` INT DEFAULT NULL,
  `holland_code` VARCHAR(10) DEFAULT NULL,
  `personality_summary` TEXT DEFAULT NULL,
  `recommended_majors` JSON DEFAULT NULL,
  `ai_full_report` MEDIUMTEXT DEFAULT NULL,
  `question_count` INT NOT NULL DEFAULT 0,
  `duration_seconds` INT DEFAULT NULL,
  `is_completed` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `report_type` VARCHAR(30) NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `summary` VARCHAR(500) DEFAULT NULL,
  `content_html` MEDIUMTEXT DEFAULT NULL,
  `content_markdown` MEDIUMTEXT DEFAULT NULL,
  `related_data` JSON DEFAULT NULL,
  `pdf_url` VARCHAR(500) DEFAULT NULL,
  `is_generated` TINYINT NOT NULL DEFAULT 0,
  `view_count` INT NOT NULL DEFAULT 0,
  `share_code` VARCHAR(50) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `school_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `school_code` VARCHAR(20) NOT NULL,
  `school_name` VARCHAR(100) NOT NULL,
  `english_name` VARCHAR(200) DEFAULT NULL,
  `school_type` VARCHAR(50) DEFAULT NULL,
  `school_nature` VARCHAR(20) DEFAULT NULL,
  `school_level` VARCHAR(30) DEFAULT NULL,
  `location` VARCHAR(100) DEFAULT NULL,
  `city` VARCHAR(50) DEFAULT NULL,
  `website` VARCHAR(200) DEFAULT NULL,
  `logo_url` VARCHAR(500) DEFAULT NULL,
  `cover_url` VARCHAR(500) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `featured_majors` JSON DEFAULT NULL,
  `has_graduate_school` TINYINT DEFAULT 0,
  `is_211` TINYINT NOT NULL DEFAULT 0,
  `is_985` TINYINT NOT NULL DEFAULT 0,
  `is_double_first` TINYINT NOT NULL DEFAULT 0,
  `founded_year` INT DEFAULT NULL,
  `student_count` INT DEFAULT NULL,
  `favorite_count` INT NOT NULL DEFAULT 0,
  `view_count` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_code` (`school_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `major_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `major_code` VARCHAR(20) NOT NULL,
  `major_name` VARCHAR(100) NOT NULL,
  `major_category` VARCHAR(50) NOT NULL,
  `major_subcategory` VARCHAR(50) DEFAULT NULL,
  `degree_type` VARCHAR(20) DEFAULT NULL,
  `study_duration` INT DEFAULT 4,
  `description` TEXT DEFAULT NULL,
  `course_list` TEXT DEFAULT NULL,
  `career_direction` TEXT DEFAULT NULL,
  `graduate_destinations` TEXT DEFAULT NULL COMMENT '毕业生去向（升学/就业结构等）',
  `typical_employers` TEXT DEFAULT NULL COMMENT '典型就业单位，顿号分隔',
  `typical_careers` TEXT DEFAULT NULL COMMENT '典型职业岗位，顿号分隔',
  `salary_avg` INT DEFAULT NULL,
  `salary_5year` INT DEFAULT NULL,
  `employment_rate` DECIMAL(5,2) DEFAULT NULL,
  `difficulty_level` VARCHAR(10) DEFAULT NULL,
  `gender_ratio` VARCHAR(20) DEFAULT NULL,
  `hot_index` INT NOT NULL DEFAULT 0,
  `favorite_count` INT NOT NULL DEFAULT 0,
  `view_count` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_major_code` (`major_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `admission_score` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `school_code` VARCHAR(20) NOT NULL,
  `school_name` VARCHAR(100) NOT NULL,
  `major_code` VARCHAR(20) DEFAULT NULL,
  `major_name` VARCHAR(100) DEFAULT NULL,
  `year` INT NOT NULL,
  `province` VARCHAR(50) NOT NULL,
  `subject_type` VARCHAR(20) NOT NULL,
  `batch` VARCHAR(20) NOT NULL,
  `min_score` INT NOT NULL,
  `avg_score` INT DEFAULT NULL,
  `max_score` INT DEFAULT NULL,
  `min_rank` INT DEFAULT NULL,
  `admission_count` INT DEFAULT NULL,
  `province_line` INT DEFAULT NULL,
  `data_source` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_school_year` (`school_code`, `year`),
  KEY `idx_province_year` (`province`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `score_segment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `year` INT NOT NULL,
  `province` VARCHAR(50) NOT NULL,
  `subject_type` VARCHAR(20) NOT NULL,
  `score` INT NOT NULL,
  `segment_count` INT DEFAULT NULL COMMENT '本段人数',
  `cumulative_rank` INT NOT NULL COMMENT '累计位次',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seg` (`year`, `province`, `subject_type`, `score`),
  KEY `idx_prov_year` (`province`, `year`, `subject_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `volunteer_wizard_progress` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `current_step` VARCHAR(32) NOT NULL DEFAULT 'ENTER_SCORE',
  `completed_steps` VARCHAR(500) DEFAULT NULL COMMENT 'JSON数组',
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

CREATE TABLE IF NOT EXISTS `province_score_line` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `year` INT NOT NULL,
  `province` VARCHAR(50) NOT NULL,
  `subject_type` VARCHAR(20) NOT NULL,
  `batch` VARCHAR(20) NOT NULL,
  `score` INT NOT NULL,
  `rank` INT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_year_province_batch` (`year`, `province`, `subject_type`, `batch`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `employment_trend` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `major_category` VARCHAR(50) NOT NULL,
  `year` INT NOT NULL,
  `employment_rate` DECIMAL(5,2) DEFAULT NULL,
  `avg_salary` INT DEFAULT NULL,
  `job_openings` INT DEFAULT NULL,
  `growth_rate` DECIMAL(5,2) DEFAULT NULL,
  `top_industry` VARCHAR(100) DEFAULT NULL,
  `top_city` VARCHAR(100) DEFAULT NULL,
  `trend_direction` VARCHAR(20) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_year` (`major_category`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `major_hot_rank` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `major_code` VARCHAR(20) NOT NULL,
  `major_name` VARCHAR(100) NOT NULL,
  `year` INT NOT NULL,
  `hot_score` DECIMAL(5,2) NOT NULL,
  `rank` INT NOT NULL,
  `search_volume` INT DEFAULT NULL,
  `apply_growth` DECIMAL(5,2) DEFAULT NULL,
  `ai_analysis` TEXT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_year_rank` (`year`, `rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `action` VARCHAR(50) NOT NULL,
  `target` VARCHAR(100) DEFAULT NULL,
  `target_id` BIGINT DEFAULT NULL,
  `ip_address` VARCHAR(50) DEFAULT NULL,
  `user_agent` VARCHAR(500) DEFAULT NULL,
  `detail` VARCHAR(1000) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
