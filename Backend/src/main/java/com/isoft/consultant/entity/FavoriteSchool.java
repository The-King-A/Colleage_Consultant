package com.isoft.consultant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("favorite_school")
public class FavoriteSchool {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String schoolCode;
    private String schoolName;
    private String schoolType;
    private String location;
    private String logoUrl;
    private String note;
    private LocalDateTime createTime;
}
