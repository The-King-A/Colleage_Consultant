package com.isoft.consultant.controller;

import com.isoft.consultant.common.Result;
import com.isoft.consultant.dto.MajorCompareItemVO;
import com.isoft.consultant.dto.MajorDetailVO;
import com.isoft.consultant.dto.MajorListItemVO;
import com.isoft.consultant.dto.PageResult;
import com.isoft.consultant.service.MajorService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/majors")
public class MajorController {

    private final MajorService majorService;

    public MajorController(MajorService majorService) {
        this.majorService = majorService;
    }

    @GetMapping("/page")
    public Result<PageResult<MajorListItemVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        return Result.success(majorService.pageMajors(keyword, category, page, size));
    }

    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.success(majorService.listCategories());
    }

    @GetMapping("/options")
    public Result<List<MajorListItemVO>> options(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String codes,
            @RequestParam(defaultValue = "200") int limit) {
        return Result.success(majorService.listOptions(keyword, codes, limit));
    }

    @GetMapping("/compare")
    public Result<List<MajorCompareItemVO>> compare(@RequestParam String codes) {
        List<String> codeList = Arrays.stream(codes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (codeList.isEmpty()) {
            return Result.badRequest("请至少选择一个专业");
        }
        return Result.success(majorService.compare(codeList));
    }

    @GetMapping("/{code}")
    public Result<MajorDetailVO> detail(@PathVariable("code") String code) {
        try {
            return Result.success(majorService.getDetail(code));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }
}
