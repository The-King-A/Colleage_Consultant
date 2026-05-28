package com.isoft.consultant.dto;

import lombok.Data;

@Data
public class WizardStepStatusVO {

    private String step;
    private String label;
    private int order;
    private boolean completed;
    private boolean current;
}
