package com.isoft.consultant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isoft.consultant.entity.SchoolInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SchoolInfoMapper extends BaseMapper<SchoolInfo> {

    @Select("SELECT DISTINCT location FROM school_info WHERE status = 1 AND location IS NOT NULL ORDER BY location")
    List<String> selectDistinctLocations();
}
