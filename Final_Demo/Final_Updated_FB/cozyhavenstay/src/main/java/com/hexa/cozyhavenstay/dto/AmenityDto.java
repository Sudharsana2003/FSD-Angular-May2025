package com.hexa.cozyhavenstay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmenityDto {
    private Integer amenityId;
    private String amenityName;
    private String description;
}