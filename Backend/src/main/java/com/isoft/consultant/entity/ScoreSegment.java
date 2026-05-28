package com.isoft.consultant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 一分一段表：分数 → 累计位次（人数越少位次越靠前）。
 */
@Data
@TableName("score_segment")
public class ScoreSegment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer year;
    private String province;
    private String subjectType;
    private Integer score;

    @TableField("segment_count")
    private Integer segmentCount;

    @TableField("cumulative_rank")
    private Integer cumulativeRank;

    private LocalDateTime createTime;
}
