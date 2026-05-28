package com.isoft.consultant.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 为已存在的 major_info 表补充就业去向相关字段（CREATE IF NOT EXISTS 不会改老表结构）。
 */
@Component
public class MajorInfoSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(MajorInfoSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public MajorInfoSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void ensureCareerColumns() {
        if (!tableExists("major_info")) {
            return;
        }
        addColumnIfMissing("graduate_destinations", "TEXT NULL COMMENT '毕业生去向'");
        addColumnIfMissing("typical_employers", "TEXT NULL COMMENT '典型就业单位'");
        addColumnIfMissing("typical_careers", "TEXT NULL COMMENT '典型职业岗位'");
    }

    private boolean tableExists(String table) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class,
                table);
        return count != null && count > 0;
    }

    private void addColumnIfMissing(String column, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'major_info' AND COLUMN_NAME = ?",
                Integer.class,
                column);
        if (count != null && count > 0) {
            return;
        }
        try {
            jdbcTemplate.execute("ALTER TABLE major_info ADD COLUMN " + column + " " + definition);
            log.info("major_info 已添加字段: {}", column);
        } catch (Exception e) {
            log.error("major_info 添加字段 {} 失败: {}", column, e.getMessage());
            throw e;
        }
    }
}
