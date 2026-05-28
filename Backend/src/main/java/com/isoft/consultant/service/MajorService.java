package com.isoft.consultant.service;

import com.isoft.consultant.dto.MajorCompareItemVO;
import com.isoft.consultant.dto.MajorDetailVO;
import com.isoft.consultant.dto.MajorListItemVO;
import com.isoft.consultant.dto.PageResult;

import java.util.List;

public interface MajorService {

    PageResult<MajorListItemVO> pageMajors(String keyword, String category, int page, int size);

    MajorDetailVO getDetail(String majorCode);

    List<MajorCompareItemVO> compare(List<String> majorCodes);

    List<String> listCategories();

    List<MajorListItemVO> listOptions(String keyword, String codes, int limit);
}
