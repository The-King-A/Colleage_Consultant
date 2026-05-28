package com.isoft.consultant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isoft.consultant.common.CityProvinceResolver;
import com.isoft.consultant.dto.FavoriteSchoolVO;
import com.isoft.consultant.entity.FavoriteSchool;
import com.isoft.consultant.entity.SchoolInfo;
import com.isoft.consultant.mapper.FavoriteSchoolMapper;
import com.isoft.consultant.mapper.SchoolInfoMapper;
import com.isoft.consultant.service.FavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteSchoolMapper favoriteSchoolMapper;
    private final SchoolInfoMapper schoolInfoMapper;
    private final CityProvinceResolver cityProvinceResolver;

    public FavoriteServiceImpl(
            FavoriteSchoolMapper favoriteSchoolMapper,
            SchoolInfoMapper schoolInfoMapper,
            CityProvinceResolver cityProvinceResolver) {
        this.favoriteSchoolMapper = favoriteSchoolMapper;
        this.schoolInfoMapper = schoolInfoMapper;
        this.cityProvinceResolver = cityProvinceResolver;
    }

    @Override
    public List<FavoriteSchoolVO> listSchools(Long userId) {
        List<FavoriteSchool> rows = favoriteSchoolMapper.selectList(
                new LambdaQueryWrapper<FavoriteSchool>()
                        .eq(FavoriteSchool::getUserId, userId)
                        .orderByDesc(FavoriteSchool::getCreateTime));
        return rows.stream().map(this::toVo).collect(Collectors.toList());
    }

    @Override
    public void addSchool(Long userId, String schoolCode, String note) {
        SchoolInfo school = schoolInfoMapper.selectOne(
                new LambdaQueryWrapper<SchoolInfo>()
                        .eq(SchoolInfo::getSchoolCode, schoolCode)
                        .eq(SchoolInfo::getStatus, 1));
        if (school == null) {
            throw new IllegalArgumentException("院校不存在");
        }
        if (isFavorite(userId, schoolCode)) {
            return;
        }
        FavoriteSchool fav = new FavoriteSchool();
        fav.setUserId(userId);
        fav.setSchoolCode(school.getSchoolCode());
        fav.setSchoolName(school.getSchoolName());
        fav.setSchoolType(school.getSchoolType());
        fav.setLocation(cityProvinceResolver.resolveProvince(school.getLocation(), school.getCity()));
        fav.setLogoUrl(school.getLogoUrl());
        fav.setNote(note);
        favoriteSchoolMapper.insert(fav);
    }

    @Override
    public void removeSchool(Long userId, String schoolCode) {
        favoriteSchoolMapper.delete(
                new LambdaQueryWrapper<FavoriteSchool>()
                        .eq(FavoriteSchool::getUserId, userId)
                        .eq(FavoriteSchool::getSchoolCode, schoolCode));
    }

    @Override
    public boolean isFavorite(Long userId, String schoolCode) {
        return favoriteSchoolMapper.selectCount(
                new LambdaQueryWrapper<FavoriteSchool>()
                        .eq(FavoriteSchool::getUserId, userId)
                        .eq(FavoriteSchool::getSchoolCode, schoolCode)) > 0;
    }

    private FavoriteSchoolVO toVo(FavoriteSchool row) {
        FavoriteSchoolVO vo = new FavoriteSchoolVO();
        vo.setId(row.getId());
        vo.setSchoolCode(row.getSchoolCode());
        vo.setSchoolName(row.getSchoolName());
        vo.setSchoolType(row.getSchoolType());
        vo.setLocation(row.getLocation());
        vo.setNote(row.getNote());
        vo.setCreateTime(row.getCreateTime());
        SchoolInfo school = schoolInfoMapper.selectOne(
                new LambdaQueryWrapper<SchoolInfo>()
                        .eq(SchoolInfo::getSchoolCode, row.getSchoolCode())
                        .last("LIMIT 1"));
        if (school != null) {
            vo.setCity(school.getCity());
            if (!StringUtils.hasText(vo.getLocation())) {
                vo.setLocation(cityProvinceResolver.resolveProvince(school.getLocation(), school.getCity()));
            }
            vo.setIs985(school.getIs985() != null && school.getIs985() == 1);
            vo.setIs211(school.getIs211() != null && school.getIs211() == 1);
        }
        return vo;
    }
}
