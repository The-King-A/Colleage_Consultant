package com.isoft.consultant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("province_score_line")
public class ProvinceScoreLine {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer year;
    private String province;
    private String subjectType;
    private String batch;
    private Integer score;

    /** 列名为 MySQL 保留字 rank，需转义 */
    @TableField("`rank`")
    private Integer rankValue;

    private LocalDateTime createTime;
}
