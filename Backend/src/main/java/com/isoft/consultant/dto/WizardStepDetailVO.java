package com.isoft.consultant.dto;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class WizardStepDetailVO {

    private String step;
    private String label;
    private int order;
    private boolean completed;
    private boolean current;
    /** 是否已有可展示/可编辑的数据 */
    private boolean hasData;
    private List<String> summary;
    /** 供前端表单回填 */
    private Map<String, Object> data = new LinkedHashMap<>();
}
