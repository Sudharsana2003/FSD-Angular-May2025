package com.hexaware.tasks.app_root.dto; // CORRECTED package declaration

import com.hexaware.tasks.app_root.entity.Priority; // CORRECTED import
import com.hexaware.tasks.app_root.entity.Status; // CORRECTED import
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDTO {

    @NotBlank(message = "Title is mandatory")
    private String title;

    private String description;

    @NotNull(message = "Due date is mandatory")
    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    @NotNull(message = "Priority is mandatory")
    private Priority priority;

    @NotNull(message = "Status is mandatory")
    private Status status;
}