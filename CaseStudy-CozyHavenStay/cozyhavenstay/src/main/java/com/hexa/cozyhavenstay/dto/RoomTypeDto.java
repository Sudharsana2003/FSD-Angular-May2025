package com.hexa.cozyhavenstay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeDto {
    private Integer roomTypeId;
    private String typeName;
    private String description;
}