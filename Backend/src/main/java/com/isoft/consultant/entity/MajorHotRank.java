package com.isoft.consultant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("major_hot_rank")
public class MajorHotRank {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String majorCode;
    private String majorName;
    private Integer year;
    private BigDecimal hotScore;

    @TableField("`rank`")
    private Integer rankValue;

    private Integer searchVolume;
    private BigDecimal applyGrowth;
    private LocalDateTime createTime;
}
