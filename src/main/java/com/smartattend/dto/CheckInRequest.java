package com.smartattend.dto;

import lombok.Data;

@Data
public class CheckInRequest {
    private Long userId;
    private String imageBase64;
}
