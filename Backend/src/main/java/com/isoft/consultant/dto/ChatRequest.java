package com.isoft.consultant.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String memoryId;
    private String message;
    private boolean search;
}
