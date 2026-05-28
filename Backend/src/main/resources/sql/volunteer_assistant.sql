-- ============================================================
-- 智选未来 · 高考志愿助手 — 完整数据库设计
-- 数据库名: volunteer_assistant
-- MySQL 8.0+
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS volunteer_assistant
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE volunteer_assistant;

-- ============================================================
-- 1. 用户表 (user)
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`      VARCHAR(50)  NOT NULL COMMENT '用户名',
  `password`      VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname`      VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  `avatar`        VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `email`         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone`         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `gender`        TINYINT      DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
  `province`      VARCHAR(50)  DEFAULT NULL COMMENT '所在省份',
  `city`          VARCHAR(50)  DEFAULT NULL COMMENT '所在城市',
  `role`          VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: USER-普通用户 ADMIN-管理员',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
  `last_login_at` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50)  DEFAULT NULL COMMENT '最后登录IP',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_province` (`province`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';


-- ============================================================
-- 2. 聊天会话表 (chat_session)
-- ============================================================
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
  `session_key` VARCHAR(100) NOT NULL COMMENT '会话唯一标识(对应Redis memoryId)',
  `title`       VARCHAR(200) DEFAULT '新对话' COMMENT '会话标题',
  `message_count` INT        NOT NULL DEFAULT 0 COMMENT '消息数量',
  `last_message` VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息摘要',
  `is_pinned`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0 COMMENT '软删除: 0-否 1-是',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_key` (`session_key`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_create` (`user_id`, `create_time` DESC),
  KEY `idx_user_deleted` (`user_id`, `is_deleted`),
  CONSTRAINT `fk_session_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';


-- ============================================================
-- 3. 聊天消息表 (chat_message)
-- ============================================================
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id`   BIGINT        NOT NULL COMMENT '会话ID',
  `user_id`      BIGINT        NOT NULL COMMENT '用户ID',
  `role`         VARCHAR(20)   NOT NULL COMMENT '角色: user-用户 assistant-AI system-系统',
  `content`      TEXT          NOT NULL COMMENT '消息内容',
  `token_count`  INT           DEFAULT NULL COMMENT 'Token消耗数量',
  `search_enabled` TINYINT     NOT NULL DEFAULT 0 COMMENT '是否启用联网搜索',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_time` (`session_id`, `create_time`),
  CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_message_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';


-- ============================================================
-- 4. 志愿推荐记录表 (recommend_record)
-- ============================================================
DROP TABLE IF EXISTS `recommend_record`;
CREATE TABLE `recommend_record` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
  `name`            VARCHAR(50)  DEFAULT NULL COMMENT '考生姓名',
  `gender`          TINYINT      DEFAULT NULL COMMENT '性别: 1-男 2-女',
  `province`        VARCHAR(50)  DEFAULT NULL COMMENT '所在省份',
  `score`           INT          NOT NULL COMMENT '高考分数',
  `subject_type`    VARCHAR(20)  NOT NULL COMMENT '科目类型: science-理科 liberal-文科 comprehensive-综合',
  `interested_majors` VARCHAR(500) DEFAULT NULL COMMENT '意向专业(JSON数组)',
  `preferred_regions` VARCHAR(500) DEFAULT NULL COMMENT '意向地区(JSON数组)',
  `other_requirements` TEXT       DEFAULT NULL COMMENT '其他要求',
  `ai_result`       MEDIUMTEXT   DEFAULT NULL COMMENT 'AI推荐结果全文',
  `rush_schools`    JSON         DEFAULT NULL COMMENT '冲刺院校(JSON)',
  `stable_schools`  JSON         DEFAULT NULL COMMENT '稳妥院校(JSON)',
  `safe_schools`    JSON         DEFAULT NULL COMMENT '保底院校(JSON)',
  `is_favorite`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否收藏: 0-否 1-是',
  `share_code`      VARCHAR(50)  DEFAULT NULL COMMENT '分享码',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_score` (`score`),
  KEY `idx_province_score` (`province`, `score`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_recommend_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿推荐记录表';


-- ============================================================
-- 5. 收藏院校表 (favorite_school)
-- ============================================================
DROP TABLE IF EXISTS `favorite_school`;
CREATE TABLE `favorite_school` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
  `school_code` VARCHAR(20)  NOT NULL COMMENT '院校代码',
  `school_name` VARCHAR(100) NOT NULL COMMENT '院校名称',
  `school_type` VARCHAR(50)  DEFAULT NULL COMMENT '院校类型: 985/211/双一流/普通',
  `location`    VARCHAR(100) DEFAULT NULL COMMENT '所在地',
  `logo_url`    VARCHAR(500) DEFAULT NULL COMMENT '校徽URL',
  `note`        VARCHAR(500) DEFAULT NULL COMMENT '用户备注',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_school` (`user_id`, `school_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_school_code` (`school_code`),
  CONSTRAINT `fk_fav_school_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏院校表';


-- ============================================================
-- 6. 收藏专业表 (favorite_major)
-- ============================================================
DROP TABLE IF EXISTS `favorite_major`;
CREATE TABLE `favorite_major` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id`      BIGINT       NOT NULL COMMENT '用户ID',
  `major_code`   VARCHAR(20)  NOT NULL COMMENT '专业代码',
  `major_name`   VARCHAR(100) NOT NULL COMMENT '专业名称',
  `major_category` VARCHAR(50) DEFAULT NULL COMMENT '专业类别',
  `degree_type`  VARCHAR(20)  DEFAULT NULL COMMENT '学位类型: 工学/理学/文学等',
  `note`         VARCHAR(500) DEFAULT NULL COMMENT '用户备注',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_major` (`user_id`, `major_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_major_code` (`major_code`),
  CONSTRAINT `fk_fav_major_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏专业表';


-- ============================================================
-- 7. 兴趣测评结果表 (interest_test_result)
-- ============================================================
DROP TABLE IF EXISTS `interest_test_result`;
CREATE TABLE `interest_test_result` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
  `memory_id`       VARCHAR(100) NOT NULL COMMENT '测评会话ID',
  `holland_r`       INT          DEFAULT NULL COMMENT '霍兰德R型(现实型)得分',
  `holland_i`       INT          DEFAULT NULL COMMENT '霍兰德I型(研究型)得分',
  `holland_a`       INT          DEFAULT NULL COMMENT '霍兰德A型(艺术型)得分',
  `holland_s`       INT          DEFAULT NULL COMMENT '霍兰德S型(社会型)得分',
  `holland_e`       INT          DEFAULT NULL COMMENT '霍兰德E型(企业型)得分',
  `holland_c`       INT          DEFAULT NULL COMMENT '霍兰德C型(常规型)得分',
  `holland_code`    VARCHAR(10)  DEFAULT NULL COMMENT '霍兰德代码(前三字母)',
  `personality_summary` TEXT     DEFAULT NULL COMMENT '性格特质总结',
  `recommended_majors` JSON     DEFAULT NULL COMMENT '推荐专业列表(JSON)',
  `ai_full_report`  MEDIUMTEXT   DEFAULT NULL COMMENT 'AI完整测评报告',
  `question_count`  INT          NOT NULL DEFAULT 0 COMMENT '答题数量',
  `duration_seconds` INT         DEFAULT NULL COMMENT '测评耗时(秒)',
  `is_completed`    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否完成: 0-进行中 1-已完成',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_completed` (`is_completed`),
  KEY `idx_holland_code` (`holland_code`),
  CONSTRAINT `fk_test_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兴趣测评结果表';


-- ============================================================
-- 8. AI报告表 (ai_report)
-- ============================================================
DROP TABLE IF EXISTS `ai_report`;
CREATE TABLE `ai_report` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '报告ID',
  `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
  `report_type`   VARCHAR(30)  NOT NULL COMMENT '报告类型: INTEREST-兴趣测评 VOLUNTEER-志愿方案 CAREER-职业分析 COMPREHENSIVE-综合报告',
  `title`         VARCHAR(200) NOT NULL COMMENT '报告标题',
  `summary`       VARCHAR(500) DEFAULT NULL COMMENT '报告摘要',
  `content_html`  MEDIUMTEXT   DEFAULT NULL COMMENT '报告内容(HTML格式)',
  `content_markdown` MEDIUMTEXT DEFAULT NULL COMMENT '报告内容(Markdown格式)',
  `related_data`  JSON         DEFAULT NULL COMMENT '关联数据(JSON)',
  `pdf_url`       VARCHAR(500) DEFAULT NULL COMMENT '生成的PDF文件URL',
  `is_generated`  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已生成PDF: 0-未生成 1-已生成',
  `view_count`    INT          NOT NULL DEFAULT 0 COMMENT '查看次数',
  `share_code`    VARCHAR(50)  DEFAULT NULL COMMENT '分享码',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_user_type` (`user_id`, `report_type`),
  KEY `idx_share_code` (`share_code`),
  CONSTRAINT `fk_report_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI报告表';


-- ============================================================
-- 9. 院校信息表 (school_info)  — 基础数据
-- ============================================================
DROP TABLE IF EXISTS `school_info`;
CREATE TABLE `school_info` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '院校ID',
  `school_code`   VARCHAR(20)  NOT NULL COMMENT '院校代码',
  `school_name`   VARCHAR(100) NOT NULL COMMENT '院校名称',
  `english_name`  VARCHAR(200) DEFAULT NULL COMMENT '英文名称',
  `school_type`   VARCHAR(50)  DEFAULT NULL COMMENT '类型: 985/211/双一流/省属重点/普通本科/专科',
  `school_nature` VARCHAR(20)  DEFAULT NULL COMMENT '性质: 公办/民办/中外合作',
  `school_level`  VARCHAR(30)  DEFAULT NULL COMMENT '层次: 本科/专科',
  `location`      VARCHAR(100) DEFAULT NULL COMMENT '所在地',
  `city`          VARCHAR(50)  DEFAULT NULL COMMENT '所在城市',
  `website`       VARCHAR(200) DEFAULT NULL COMMENT '官网URL',
  `logo_url`      VARCHAR(500) DEFAULT NULL COMMENT '校徽URL',
  `cover_url`     VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
  `description`   TEXT         DEFAULT NULL COMMENT '院校简介',
  `featured_majors` JSON       DEFAULT NULL COMMENT '特色专业(JSON)',
  `has_graduate_school` TINYINT DEFAULT 0 COMMENT '是否有研究生院',
  `is_211`        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否211',
  `is_985`        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否985',
  `is_double_first` TINYINT    NOT NULL DEFAULT 0 COMMENT '是否双一流',
  `founded_year`  INT          DEFAULT NULL COMMENT '建校年份',
  `student_count` INT          DEFAULT NULL COMMENT '在校生人数',
  `favorite_count` INT         NOT NULL DEFAULT 0 COMMENT '收藏数',
  `view_count`    INT          NOT NULL DEFAULT 0 COMMENT '查看数',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-正常',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_code` (`school_code`),
  KEY `idx_school_name` (`school_name`),
  KEY `idx_school_type` (`school_type`),
  KEY `idx_location` (`location`),
  KEY `idx_is_985` (`is_985`),
  KEY `idx_is_211` (`is_211`),
  KEY `idx_favorite_count` (`favorite_count` DESC),
  FULLTEXT KEY `ft_school_name` (`school_name`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='院校信息表';


-- ============================================================
-- 10. 专业信息表 (major_info)  — 基础数据
-- ============================================================
DROP TABLE IF EXISTS `major_info`;
CREATE TABLE `major_info` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '专业ID',
  `major_code`      VARCHAR(20)  NOT NULL COMMENT '专业代码',
  `major_name`      VARCHAR(100) NOT NULL COMMENT '专业名称',
  `major_category`  VARCHAR(50)  NOT NULL COMMENT '门类: 工学/理学/医学/经济学等',
  `major_subcategory` VARCHAR(50) DEFAULT NULL COMMENT '专业类: 计算机类/电子信息类等',
  `degree_type`     VARCHAR(20)  DEFAULT NULL COMMENT '授予学位: 工学学士/理学学士等',
  `study_duration`  INT          DEFAULT 4 COMMENT '学制(年)',
  `description`     TEXT         DEFAULT NULL COMMENT '专业简介',
  `course_list`     TEXT         DEFAULT NULL COMMENT '主要课程',
  `career_direction` TEXT        DEFAULT NULL COMMENT '就业方向',
  `salary_avg`      INT          DEFAULT NULL COMMENT '平均薪资(元)',
  `salary_5year`    INT          DEFAULT NULL COMMENT '5年后平均薪资(元)',
  `employment_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '就业率(%)',
  `difficulty_level` VARCHAR(10) DEFAULT NULL COMMENT '难度: easy/medium/hard',
  `gender_ratio`    VARCHAR(20)  DEFAULT NULL COMMENT '性别比例',
  `hot_index`       INT          NOT NULL DEFAULT 0 COMMENT '热度指数',
  `favorite_count`  INT          NOT NULL DEFAULT 0 COMMENT '收藏数',
  `view_count`      INT          NOT NULL DEFAULT 0 COMMENT '查看数',
  `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-正常',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_major_code` (`major_code`),
  KEY `idx_major_name` (`major_name`),
  KEY `idx_category` (`major_category`),
  KEY `idx_hot_index` (`hot_index` DESC),
  KEY `idx_favorite_count` (`favorite_count` DESC),
  FULLTEXT KEY `ft_major_name` (`major_name`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业信息表';


-- ============================================================
-- 11. 历年录取分数线表 (admission_score)
-- ============================================================
DROP TABLE IF EXISTS `admission_score`;
CREATE TABLE `admission_score` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `school_code`     VARCHAR(20)  NOT NULL COMMENT '院校代码',
  `school_name`     VARCHAR(100) NOT NULL COMMENT '院校名称',
  `major_code`      VARCHAR(20)  DEFAULT NULL COMMENT '专业代码',
  `major_name`      VARCHAR(100) DEFAULT NULL COMMENT '专业名称',
  `year`            INT          NOT NULL COMMENT '年份',
  `province`        VARCHAR(50)  NOT NULL COMMENT '省份',
  `subject_type`    VARCHAR(20)  NOT NULL COMMENT '科类: 理科/文科/综合',
  `batch`           VARCHAR(20)  NOT NULL COMMENT '批次: 本科一批/本科二批/专科批',
  `min_score`       INT          NOT NULL COMMENT '最低录取分',
  `avg_score`       INT          DEFAULT NULL COMMENT '平均录取分',
  `max_score`       INT          DEFAULT NULL COMMENT '最高录取分',
  `min_rank`        INT          DEFAULT NULL COMMENT '最低位次',
  `admission_count` INT          DEFAULT NULL COMMENT '录取人数',
  `province_line`   INT          DEFAULT NULL COMMENT '当年省控线',
  `data_source`    VARCHAR(100)  DEFAULT NULL COMMENT '数据来源',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_school_year` (`school_code`, `year`),
  KEY `idx_province_year` (`province`, `year`),
  KEY `idx_major_year` (`major_code`, `year`),
  KEY `idx_school_province` (`school_code`, `province`, `year`),
  KEY `idx_province_subject_year` (`province`, `subject_type`, `year`, `min_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='历年录取分数线表';


-- ============================================================
-- 12. 省控线表 (province_score_line)
-- ============================================================
DROP TABLE IF EXISTS `province_score_line`;
CREATE TABLE `province_score_line` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `year`         INT         NOT NULL COMMENT '年份',
  `province`     VARCHAR(50) NOT NULL COMMENT '省份',
  `subject_type` VARCHAR(20) NOT NULL COMMENT '科类: 理科/文科/综合',
  `batch`        VARCHAR(20) NOT NULL COMMENT '批次: 本科一批/本科二批/专科批',
  `score`        INT         NOT NULL COMMENT '省控线分数',
  `rank`         INT         DEFAULT NULL COMMENT '对应位次',
  `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_year_province_batch` (`year`, `province`, `subject_type`, `batch`),
  KEY `idx_province_year` (`province`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='省控线表';


-- ============================================================
-- 13. 就业趋势数据表 (employment_trend)
-- ============================================================
DROP TABLE IF EXISTS `employment_trend`;
CREATE TABLE `employment_trend` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `major_category` VARCHAR(50)  NOT NULL COMMENT '专业大类',
  `year`           INT          NOT NULL COMMENT '年份',
  `employment_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '就业率(%)',
  `avg_salary`     INT          DEFAULT NULL COMMENT '平均薪资(元)',
  `job_openings`   INT          DEFAULT NULL COMMENT '招聘岗位数(万)',
  `growth_rate`    DECIMAL(5,2) DEFAULT NULL COMMENT '同比增长率(%)',
  `top_industry`   VARCHAR(100) DEFAULT NULL COMMENT '热门行业',
  `top_city`       VARCHAR(100) DEFAULT NULL COMMENT '热门城市',
  `trend_direction` VARCHAR(20)  DEFAULT NULL COMMENT '趋势方向: up/down/stable',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_year` (`major_category`, `year`),
  KEY `idx_year` (`year`),
  KEY `idx_category` (`major_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='就业趋势数据表';


-- ============================================================
-- 14. 专业热度排行表 (major_hot_rank)
-- ============================================================
DROP TABLE IF EXISTS `major_hot_rank`;
CREATE TABLE `major_hot_rank` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `major_code`     VARCHAR(20)  NOT NULL COMMENT '专业代码',
  `major_name`     VARCHAR(100) NOT NULL COMMENT '专业名称',
  `year`           INT          NOT NULL COMMENT '年份',
  `hot_score`      DECIMAL(5,2) NOT NULL COMMENT '热度评分(0-100)',
  `rank`           INT          NOT NULL COMMENT '热度排名',
  `search_volume`  INT          DEFAULT NULL COMMENT '搜索量',
  `apply_growth`   DECIMAL(5,2) DEFAULT NULL COMMENT '报考增长(%)',
  `ai_analysis`    TEXT         DEFAULT NULL COMMENT 'AI热度分析',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_year_rank` (`year`, `rank`),
  KEY `idx_major_code` (`major_code`),
  KEY `idx_hot_score` (`hot_score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业热度排行表';


-- ============================================================
-- 15. 操作日志表 (operation_log)
-- ============================================================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id`     BIGINT       DEFAULT NULL COMMENT '用户ID',
  `action`      VARCHAR(50)  NOT NULL COMMENT '操作类型: LOGIN/REGISTER/CHAT/RECOMMEND/TEST/VIEW/MODIFY',
  `target`      VARCHAR(100) DEFAULT NULL COMMENT '操作对象',
  `target_id`   BIGINT       DEFAULT NULL COMMENT '操作对象ID',
  `ip_address`  VARCHAR(50)  DEFAULT NULL COMMENT 'IP地址',
  `user_agent`  VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA',
  `detail`      VARCHAR(1000) DEFAULT NULL COMMENT '操作详情(JSON)',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_action` (`action`),
  KEY `idx_create_time` (`create_time` DESC),
  KEY `idx_user_action_time` (`user_id`, `action`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';


-- ============================================================
-- 种子数据 (Seed Data)
-- ============================================================

-- 管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 'admin@volunteer.cn', 'ADMIN', 1);

-- 测试用户 (密码: test123456)
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `phone`, `gender`, `province`, `role`) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '测试考生', 'test@test.com', '13800000000', 1, '河南', 'USER');

-- 院校种子数据
INSERT INTO `school_info` (`school_code`, `school_name`, `school_type`, `school_nature`, `location`, `city`, `is_985`, `is_211`, `is_double_first`, `description`) VALUES
('10698', '西安交通大学', 'C9联盟', '公办', '陕西', '西安', 1, 1, 1, '西安交通大学是我国最早兴办的高等学府之一，是教育部直属重点大学，国家"211工程"和"985工程"重点建设高校，首批"双一流"建设高校。'),
('10699', '西北工业大学', '985', '公办', '陕西', '西安', 1, 1, 1, '西北工业大学是我国唯一一所以同时发展航空、航天、航海工程教育和科学研究为特色的全国重点大学。'),
('10701', '西安电子科技大学', '211', '公办', '陕西', '西安', 0, 1, 1, '西安电子科技大学是以信息与电子学科为主，工、理、管、文多学科协调发展的全国重点大学。'),
('10718', '陕西师范大学', '211', '公办', '陕西', '西安', 0, 1, 1, '陕西师范大学是教育部直属、世界一流学科建设高校，是国家培养高等院校师资的重要基地。'),
('10697', '西北大学', '211', '公办', '陕西', '西安', 0, 1, 1, '西北大学是首批国家"世界一流学科建设高校"，国家"211工程"重点建设高校。'),
('10712', '西北农林科技大学', '985', '公办', '陕西', '杨凌', 1, 1, 1, '西北农林科技大学是教育部直属、国家"985工程"和"211工程"重点建设高校，首批入选国家"世界一流大学和一流学科"建设高校。'),
('10710', '长安大学', '211', '公办', '陕西', '西安', 0, 1, 1, '长安大学是教育部直属全国重点大学，国家首批"211工程"重点建设大学，国家"985优势学科创新平台"建设高校。');

-- 专业种子数据
INSERT INTO `major_info` (`major_code`, `major_name`, `major_category`, `major_subcategory`, `degree_type`, `description`, `hot_index`, `employment_rate`, `salary_avg`) VALUES
('080901', '计算机科学与技术', '工学', '计算机类', '工学学士', '研究计算机系统结构、程序系统、人工智能以及计算应用', 98, 96.5, 15000),
('080902', '软件工程', '工学', '计算机类', '工学学士', '研究用工程化方法构建和维护有效的、实用的和高质量的软件', 96, 95.8, 16000),
('080717T', '人工智能', '工学', '计算机类', '工学学士', '研究开发用于模拟、延伸和扩展人的智能的理论、方法、技术及应用系统', 99, 94.2, 18000),
('080703', '数据科学与大数据技术', '工学', '计算机类', '工学学士', '研究大数据的采集、存储、处理、分析和应用', 95, 93.5, 15500),
('080701', '电子信息工程', '工学', '电子信息类', '工学学士', '研究电子技术和信息系统的设计、开发和应用', 90, 94.0, 14000),
('080601', '电气工程及其自动化', '工学', '电气类', '工学学士', '研究电能的产生、传输、转换、控制和应用', 85, 96.2, 13000),
('100201K', '临床医学', '医学', '临床医学类', '医学学士', '研究疾病的病因、诊断、治疗和预后，培养临床医师', 92, 97.8, 12000),
('020301K', '金融学', '经济学', '金融学类', '经济学学士', '研究价值判断和价值规律，研究金融市场的运作规律', 88, 91.5, 13500),
('120203K', '会计学', '管理学', '工商管理类', '管理学学士', '研究财务活动和成本资料的收集、分类、综合、分析和解释', 82, 93.2, 10000),
('030101K', '法学', '法学', '法学类', '法学学士', '研究法律、法律现象以及其规律性', 84, 89.5, 11000);

-- 省控线种子数据 (2023-2025 河南理科)
INSERT INTO `province_score_line` (`year`, `province`, `subject_type`, `batch`, `score`) VALUES
(2023, '河南', 'science', '本科一批', 514),
(2023, '河南', 'science', '本科二批', 409),
(2024, '河南', 'science', '本科一批', 511),
(2024, '河南', 'science', '本科二批', 407),
(2025, '河南', 'science', '本科一批', 509),
(2025, '河南', 'science', '本科二批', 405);

-- 录取分数线种子数据 (西安交通大学 计算机科学与技术 河南)
INSERT INTO `admission_score` (`school_code`, `school_name`, `major_code`, `major_name`, `year`, `province`, `subject_type`, `batch`, `min_score`, `avg_score`, `min_rank`, `admission_count`) VALUES
('10698', '西安交通大学', '080901', '计算机科学与技术', 2023, '河南', 'science', '本科一批', 646, 653, 3650, 12),
('10698', '西安交通大学', '080901', '计算机科学与技术', 2024, '河南', 'science', '本科一批', 643, 651, 3820, 14),
('10698', '西安交通大学', '080901', '计算机科学与技术', 2025, '河南', 'science', '本科一批', 648, 656, 3580, 15),
('10698', '西安交通大学', '080717T', '人工智能', 2023, '河南', 'science', '本科一批', 648, 655, 3420, 8),
('10698', '西安交通大学', '080717T', '人工智能', 2024, '河南', 'science', '本科一批', 646, 653, 3550, 10),
-- 西北工业大学
('10699', '西北工业大学', '080901', '计算机科学与技术', 2023, '河南', 'science', '本科一批', 632, 640, 5980, 18),
('10699', '西北工业大学', '080901', '计算机科学与技术', 2024, '河南', 'science', '本科一批', 630, 638, 6120, 20),
-- 西安电子科技大学
('10701', '西安电子科技大学', '080901', '计算机科学与技术', 2023, '河南', 'science', '本科一批', 615, 622, 10800, 35),
('10701', '西安电子科技大学', '080901', '计算机科学与技术', 2024, '河南', 'science', '本科一批', 613, 620, 11200, 38),
('10701', '西安电子科技大学', '080702', '电子信息工程', 2023, '河南', 'science', '本科一批', 620, 628, 9200, 28),
('10701', '西安电子科技大学', '080702', '电子信息工程', 2024, '河南', 'science', '本科一批', 618, 625, 9500, 30);

-- 就业趋势种子数据
INSERT INTO `employment_trend` (`major_category`, `year`, `employment_rate`, `avg_salary`, `job_openings`, `growth_rate`, `trend_direction`) VALUES
('计算机类', 2023, 95.2, 14500, 82, 12.5, 'up'),
('计算机类', 2024, 96.1, 15200, 95, 15.8, 'up'),
('计算机类', 2025, 96.5, 15800, 110, 16.2, 'up'),
('电子信息类', 2023, 94.0, 13500, 65, 10.2, 'up'),
('电子信息类', 2024, 94.5, 14200, 72, 11.5, 'up'),
('医学类', 2023, 97.2, 11500, 45, 8.5, 'up'),
('金融学类', 2023, 90.5, 12800, 38, 5.2, 'stable'),
('法学类', 2023, 88.0, 10500, 28, 3.5, 'stable');

-- 专业热度排行种子数据
INSERT INTO `major_hot_rank` (`major_code`, `major_name`, `year`, `hot_score`, `rank`, `search_volume`, `apply_growth`) VALUES
('080717T', '人工智能', 2025, 98.5, 1, 2850000, 22.5),
('080901', '计算机科学与技术', 2025, 97.2, 2, 3120000, 18.8),
('080902', '软件工程', 2025, 96.0, 3, 2650000, 16.5),
('080703', '数据科学与大数据技术', 2025, 94.5, 4, 1980000, 20.2),
('100201K', '临床医学', 2025, 92.8, 5, 2450000, 12.5),
('080701', '电子信息工程', 2025, 91.0, 6, 2150000, 14.0),
('020301K', '金融学', 2025, 87.5, 7, 1850000, 8.5),
('080601', '电气工程及其自动化', 2025, 86.2, 8, 1680000, 10.2);
