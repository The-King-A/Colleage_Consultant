package com.isoft.consultant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isoft.consultant.entity.AdmissionScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdmissionScoreMapper extends BaseMapper<AdmissionScore> {

    @Select("SELECT DISTINCT province FROM admission_score WHERE province IS NOT NULL ORDER BY province")
    List<String> selectDistinctProvinces();
}
