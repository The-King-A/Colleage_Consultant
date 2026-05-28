package com.isoft.consultant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("volunteer_plan")
public class VolunteerPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String schoolProvince;
    private String scoreProvince;
    private String subjectType;
    private Integer userScore;
    private String planJson;
    private String note;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
