package com.astra.load_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SegmentResponse {
    private String name;
    private String downloadUrl;
    private int sizeKB;
}
