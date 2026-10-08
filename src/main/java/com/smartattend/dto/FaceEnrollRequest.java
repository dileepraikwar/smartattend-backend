package com.smartattend.dto;

import lombok.Data;
import java.util.List;

@Data
public class FaceEnrollRequest {
    private Long userId;
    private List<String> imagesBase64;
}
