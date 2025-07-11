package com.hexaware.tasks.app_root.controller;

import com.hexaware.tasks.app_root.dto.TaskRequestDTO;
import com.hexaware.tasks.app_root.dto.TaskResponseDTO;
import com.hexaware.tasks.app_root.service.TaskService;
import jakarta.validation.Valid; // For input validation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // For HTTP status codes
import org.springframework.http.ResponseEntity; // For building HTTP responses
import org.springframework.web.bind.annotation.*; // For REST annotations

import java.util.List;
import com.hexaware.tasks.app_root.entity.User; // Import your User entity
import com.hexaware.tasks.app_root.service.CustomUserDetailsService; // Used to fetch User entity
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Import this
import org.springframework.security.core.userdetails.UserDetails; // Import this
import org.springframework.web.server.ResponseStatusException; // For better error handling

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final CustomUserDetailsService customUserDetailsService; // Inject CustomUserDetailsService

    @Autowired
    public TaskController(TaskService taskService, CustomUserDetailsService customUserDetailsService) {
        this.taskService = taskService;
        this.customUserDetailsService = customUserDetailsService;
    }

    // Helper method to get the current authenticated User entity
    private User getCurrentUser(UserDetails currentUserDetails) {
        if (currentUserDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }
        // customUserDetailsService.loadUserByUsername returns UserDetails,
        // but since your User entity implements UserDetails, you can cast it.
        return (User) customUserDetailsService.loadUserByUsername(currentUserDetails.getUsername());
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(@AuthenticationPrincipal UserDetails currentUserDetails) {
        User user = getCurrentUser(currentUserDetails);
        List<TaskResponseDTO> tasks = taskService.getAllTasks(user); // Pass the user to filter
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUserDetails) {
        User user = getCurrentUser(currentUserDetails);
        return taskService.getTaskById(id, user) // Pass the user for ownership check
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> addTask(@Valid @RequestBody TaskRequestDTO taskRequestDTO,
            @AuthenticationPrincipal UserDetails currentUserDetails) {
        User user = getCurrentUser(currentUserDetails);
        TaskResponseDTO newTask = taskService.addTask(taskRequestDTO, user); // Pass the user to assign ownership
        return new ResponseEntity<>(newTask, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id,
            @Valid @RequestBody TaskRequestDTO taskRequestDTO,
            @AuthenticationPrincipal UserDetails currentUserDetails) {
        User user = getCurrentUser(currentUserDetails);
        return taskService.updateTask(id, taskRequestDTO, user) // Pass the user for ownership check
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUserDetails) {
        User user = getCurrentUser(currentUserDetails);
        if (taskService.deleteTask(id, user)) { // Pass the user for ownership check
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}