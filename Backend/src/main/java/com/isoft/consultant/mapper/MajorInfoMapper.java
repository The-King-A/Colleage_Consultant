package com.isoft.consultant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isoft.consultant.entity.MajorInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MajorInfoMapper extends BaseMapper<MajorInfo> {

    @Select("SELECT DISTINCT major_category FROM major_info WHERE status = 1 ORDER BY major_category")
    List<String> selectDistinctCategories();
}
