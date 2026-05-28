package com.isoft.consultant.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final DataSource dataSource;
    private final MajorInfoSchemaMigration majorInfoSchemaMigration;

    /** 本地已手动导库时保持 false，避免每次启动执行 schema.sql / 演示种子 */
    @Value("${consultant.data.run-sql-on-startup:false}")
    private boolean runSqlOnStartup;

    public DataInitializer(DataSource dataSource, MajorInfoSchemaMigration majorInfoSchemaMigration) {
        this.dataSource = dataSource;
        this.majorInfoSchemaMigration = majorInfoSchemaMigration;
    }

    @Override
    public void run(String... args) {
        try (Connection conn = dataSource.getConnection()) {
            // 老库补字段（轻量 DDL，非 classpath SQL 脚本）
            majorInfoSchemaMigration.ensureCareerColumns();

            if (!runSqlOnStartup) {
                return;
            }

            // Step 1: ensure tables exist (safe CREATE IF NOT EXISTS)
            runSql(conn, "sql/schema.sql");

            // Step 2: 小规模演示种子（全国本科请手动执行 sql/import_china_undergraduate_schools.sql）
            int count = countRows(conn, "school_info");
            if (count >= 15) {
                log.info("数据库已有 {} 所院校，跳过演示种子（全国本科请执行 import_china_undergraduate_schools.sql）", count);
                return;
            }

            log.info("院校数据不足（{}所），导入演示种子...", count);
            runSql(conn, "sql/volunteer_assistant_seed.sql");
            runSql(conn, "sql/seed_more_data.sql");
            runSql(conn, "sql/seed_south_china_data.sql");
            log.info("演示种子完成，共 {} 所。完整本科名单请执行 sql/import_china_undergraduate_schools.sql", countRows(conn, "school_info"));

        } catch (Exception e) {
            log.error("数据初始化异常: {}", e.getMessage());
        }
    }

    private void runSql(Connection conn, String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (resource.exists()) {
                ScriptUtils.executeSqlScript(conn, resource);
                log.info("  SQL已执行: {}", path);
            }
        } catch (Exception e) {
            log.debug("  跳过 {}: {}", path, e.getMessage());
        }
    }

    private int countRows(Connection conn, String table) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
