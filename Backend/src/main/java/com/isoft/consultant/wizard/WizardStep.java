package com.isoft.consultant.wizard;

import java.util.Arrays;
import java.util.List;

public enum WizardStep {

    ENTER_SCORE("录入成绩", 1),
    INTEREST_TEST("兴趣测评", 2),
    MATCH("冲稳保测算", 3),
    FAVORITES("收藏院校", 4),
    FINALIZE("定稿方案", 5);

    private final String label;
    private final int order;

    WizardStep(String label, int order) {
        this.label = label;
        this.order = order;
    }

    public String getLabel() {
        return label;
    }

    public int getOrder() {
        return order;
    }

    public static List<WizardStep> flow() {
        return Arrays.asList(values());
    }

    public WizardStep next() {
        int idx = ordinal();
        if (idx >= values().length - 1) {
            return this;
        }
        return values()[idx + 1];
    }
}
