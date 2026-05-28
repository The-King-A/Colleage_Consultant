package com.isoft.consultant.controller;

import com.isoft.consultant.common.Result;
import com.isoft.consultant.dto.PageResult;
import com.isoft.consultant.dto.SchoolCompareItemVO;
import com.isoft.consultant.dto.SchoolDetailVO;
import com.isoft.consultant.dto.SchoolListItemVO;
import com.isoft.consultant.dto.SchoolMapMarkersVO;
import com.isoft.consultant.service.ProvincialAdmissionCoverageService;
import com.isoft.consultant.service.SchoolService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schools")
public class SchoolController {

    private final SchoolService schoolService;
    private final ProvincialAdmissionCoverageService admissionCoverageService;

    public SchoolController(
            SchoolService schoolService,
            ProvincialAdmissionCoverageService admissionCoverageService) {
        this.schoolService = schoolService;
        this.admissionCoverageService = admissionCoverageService;
    }

    @GetMapping("/page")
    public Result<PageResult<SchoolListItemVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean is985,
            @RequestParam(required = false) Boolean is211,
            @RequestParam(required = false, defaultValue = "河南") String scoreProvince,
            @RequestParam(required = false, defaultValue = "science") String subjectType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        return Result.success(schoolService.pageSchools(
                keyword, location, is985, is211, scoreProvince, subjectType, page, size));
    }

    @GetMapping("/options")
    public Result<List<SchoolListItemVO>> options(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String codes,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "河南") String scoreProvince,
            @RequestParam(required = false, defaultValue = "science") String subjectType,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(schoolService.listOptions(
                keyword, codes, location, scoreProvince, subjectType, limit));
    }

    @GetMapping("/locations")
    public Result<List<String>> locations() {
        return Result.success(schoolService.listLocations());
    }

    @GetMapping("/score-provinces")
    public Result<List<String>> scoreProvinces() {
        return Result.success(schoolService.listScoreProvinces());
    }

    @GetMapping("/compare")
    public Result<List<SchoolCompareItemVO>> compare(
            @RequestParam String codes,
            @RequestParam(required = false, defaultValue = "河南") String scoreProvince,
            @RequestParam(required = false, defaultValue = "science") String subjectType,
            @RequestParam(required = false) String years) {
        List<String> codeList = Arrays.stream(codes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (codeList.isEmpty()) {
            return Result.badRequest("请至少选择一所院校");
        }

        List<Integer> yearList = null;
        if (years != null && !years.isBlank()) {
            yearList = Arrays.stream(years.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }

        return Result.success(schoolService.compare(codeList, scoreProvince, subjectType, yearList));
    }

    /**
     * 补全冲稳保测算所需的省内 + 跨省录取样本（可传 province=河南 仅补一省）
     */
    @PostMapping("/ensure-matcher-data")
    public Result<Map<String, Object>> ensureMatcherData(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String schoolProvince,
            @RequestParam(required = false) String scoreProvince) {
        String schoolProv = schoolProvince != null ? schoolProvince.trim()
                : (province != null ? province.trim() : null);
        String scoreProv = scoreProvince != null ? scoreProvince.trim() : schoolProv;
        if (schoolProv == null && scoreProv == null) {
            schoolProv = null;
        } else if (schoolProv == null) {
            schoolProv = scoreProv;
        } else if (scoreProv == null) {
            scoreProv = schoolProv;
        }
        int inserted = (schoolProv == null && scoreProv == null)
                ? admissionCoverageService.ensureLocalCoverage(null)
                : admissionCoverageService.ensureMatcherPair(schoolProv, scoreProv);
        return Result.success(Map.of(
                "inserted", inserted,
                "schoolProvince", schoolProv != null ? schoolProv : "",
                "scoreProvince", scoreProv != null ? scoreProv : "",
                "message", inserted > 0 ? "已补全录取样本数据，请重新测算" : "样本数据已充足，无需补全"));
    }

    @GetMapping("/map-markers")
    public Result<SchoolMapMarkersVO> mapMarkers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean is985,
            @RequestParam(required = false) Boolean is211,
            @RequestParam(required = false, defaultValue = "河南") String scoreProvince,
            @RequestParam(required = false, defaultValue = "science") String subjectType,
            @RequestParam(defaultValue = "500") int limit) {
        return Result.success(schoolService.listMapMarkers(
                keyword, location, is985, is211, scoreProvince, subjectType, limit));
    }

    @GetMapping("/{code}")
    public Result<SchoolDetailVO> detail(
            @PathVariable("code") String code,
            @RequestParam(required = false, defaultValue = "河南") String scoreProvince,
            @RequestParam(required = false, defaultValue = "science") String subjectType) {
        try {
            return Result.success(schoolService.getDetail(code, scoreProvince, subjectType));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }
}
