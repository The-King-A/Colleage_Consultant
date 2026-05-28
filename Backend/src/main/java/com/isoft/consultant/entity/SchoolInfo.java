package com.isoft.consultant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("school_info")
public class SchoolInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String schoolCode;
    private String schoolName;
    private String englishName;
    private String schoolType;
    private String schoolNature;
    private String schoolLevel;
    private String location;
    private String city;
    private String website;
    private String logoUrl;
    private String coverUrl;
    private String description;
    private String featuredMajors;

    @TableField("has_graduate_school")
    private Integer hasGraduateSchool;

    @TableField("is_211")
    private Integer is211;

    @TableField("is_985")
    private Integer is985;

    @TableField("is_double_first")
    private Integer isDoubleFirst;

    private Integer foundedYear;
    private Integer studentCount;
    private Integer favoriteCount;
    private Integer viewCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
