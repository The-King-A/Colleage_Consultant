package com.isoft.consultant.dto;

import lombok.Data;

@Data
public class ExportReviewPdfRequest {
    private String title;
    private String meta;
    private String content;
}
