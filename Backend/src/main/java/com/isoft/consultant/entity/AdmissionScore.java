package com.isoft.consultant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admission_score")
public class AdmissionScore {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String schoolCode;
    private String schoolName;
    private String majorCode;
    private String majorName;
    private Integer year;
    private String province;
    private String subjectType;
    private String batch;
    private Integer minScore;
    private Integer avgScore;
    private Integer maxScore;
    private Integer minRank;
    private Integer admissionCount;
    private Integer provinceLine;
    private String dataSource;
    private LocalDateTime createTime;
}
