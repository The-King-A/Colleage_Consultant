package com.isoft.consultant.service;

import com.isoft.consultant.dto.FavoriteSchoolVO;

import java.util.List;

public interface FavoriteService {

    List<FavoriteSchoolVO> listSchools(Long userId);

    void addSchool(Long userId, String schoolCode, String note);

    void removeSchool(Long userId, String schoolCode);

    boolean isFavorite(Long userId, String schoolCode);
}
