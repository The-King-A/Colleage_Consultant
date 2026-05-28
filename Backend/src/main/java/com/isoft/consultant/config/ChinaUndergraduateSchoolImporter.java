package com.isoft.consultant.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isoft.consultant.entity.SchoolInfo;
import com.isoft.consultant.mapper.SchoolInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 从 classpath:data/china_undergraduate_schools.csv 导入全国本科院校（教育部名单）。
 * 默认关闭，需配置 consultant.data.import-undergraduate-schools=true 才会在启动时执行。
 * 推荐手动执行 sql/import_china_undergraduate_schools.sql。
 */
@Component
@Order(10)
public class ChinaUndergraduateSchoolImporter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ChinaUndergraduateSchoolImporter.class);
    private static final String CSV_PATH = "data/china_undergraduate_schools.csv";

    private final SchoolInfoMapper schoolInfoMapper;

    @Value("${consultant.data.import-undergraduate-schools:false}")
    private boolean importOnStartup;

    public ChinaUndergraduateSchoolImporter(SchoolInfoMapper schoolInfoMapper) {
        this.schoolInfoMapper = schoolInfoMapper;
    }

    @Override
    public void run(String... args) {
        if (!importOnStartup) {
            return;
        }
        try {
            long existing = schoolInfoMapper.selectCount(
                    new LambdaQueryWrapper<SchoolInfo>().eq(SchoolInfo::getStatus, 1));
            if (existing >= 1200) {
                log.info("院校库已有 {} 所，跳过 CSV 本科导入", existing);
                return;
            }
            int inserted = importFromCsv();
            log.info("全国本科院校 CSV 导入完成，新增 {} 所，当前共约 {} 所",
                    inserted, existing + inserted);
        } catch (Exception e) {
            log.warn("全国本科院校 CSV 导入失败: {}", e.getMessage());
        }
    }

    public int importFromCsv() throws Exception {
        ClassPathResource resource = new ClassPathResource(CSV_PATH);
        if (!resource.exists()) {
            log.warn("未找到 {}", CSV_PATH);
            return 0;
        }
        int inserted = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                return 0;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (!StringUtils.hasText(line)) {
                    continue;
                }
                List<String> cols = parseCsvLine(line);
                if (cols.size() < 10) {
                    continue;
                }
                String code = cols.get(0).trim();
                if (!StringUtils.hasText(code)) {
                    continue;
                }
                if (schoolInfoMapper.selectCount(new LambdaQueryWrapper<SchoolInfo>()
                        .eq(SchoolInfo::getSchoolCode, code)) > 0) {
                    continue;
                }
                SchoolInfo school = new SchoolInfo();
                school.setSchoolCode(code);
                school.setSchoolName(cols.get(1).trim());
                school.setLocation(cols.get(2).trim());
                school.setCity(cols.get(3).trim());
                school.setSchoolNature(cols.get(4).trim());
                school.setSchoolType("本科");
                school.setSchoolLevel(cols.get(5).trim());
                school.setIs985(parseInt(cols.get(6)));
                school.setIs211(parseInt(cols.get(7)));
                school.setIsDoubleFirst(parseInt(cols.get(8)));
                school.setDescription(cols.get(9).trim());
                school.setStatus(1);
                schoolInfoMapper.insert(school);
                inserted++;
            }
        }
        return inserted;
    }

    private static int parseInt(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /** 简单 CSV 解析（支持引号内逗号） */
    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        result.add(cur.toString());
        return result;
    }
}
