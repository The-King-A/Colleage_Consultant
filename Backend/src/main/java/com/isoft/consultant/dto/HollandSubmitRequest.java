package com.isoft.consultant.dto;

import lombok.Data;

import java.util.List;

@Data
public class HollandSubmitRequest {
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Integer questionId;
        /** 1=非常不符合 … 5=非常符合 */
        private Integer score;
    }
}
