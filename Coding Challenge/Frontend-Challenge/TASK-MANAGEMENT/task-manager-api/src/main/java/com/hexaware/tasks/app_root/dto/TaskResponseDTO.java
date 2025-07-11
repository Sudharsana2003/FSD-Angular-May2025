package com.hexaware.tasks.app_root.dto; // CORRECTED package declaration

import com.hexaware.tasks.app_root.entity.Priority; // CORRECTED import
import com.hexaware.tasks.app_root.entity.Status; // CORRECTED import
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {

    private Long id;

    private String title;

    private String description;

    private LocalDate dueDate;

    private Priority priority;

    private Status status;
}