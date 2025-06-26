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

@RestController // Marks this class as a REST controller, handling web requests.
@RequestMapping("/api/tasks") // Base URL path for all endpoints defined in this controller.
public class TaskController {

    private final TaskService taskService;

    @Autowired // Injects the TaskService dependency. Spring automatically provides an instance.
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping // Maps HTTP GET requests to "/api/tasks".
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {
        List<TaskResponseDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks); // Returns a 200 OK status with the list of tasks.
    }

    @GetMapping("/{id}") // Maps HTTP GET requests to "/api/tasks/{id}". {id} is a path variable.
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        // Uses Optional to handle cases where the task might not be found.
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok) // If task is found, return 200 OK with the task.
                .orElse(ResponseEntity.notFound().build()); // If not found, return 404 Not Found.
    }

    @PostMapping // Maps HTTP POST requests to "/api/tasks".
    // @Valid: Triggers validation checks defined in TaskRequestDTO.
    // @RequestBody: Binds the HTTP request body to the TaskRequestDTO object.
    public ResponseEntity<TaskResponseDTO> addTask(@Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        TaskResponseDTO newTask = taskService.addTask(taskRequestDTO);
        return new ResponseEntity<>(newTask, HttpStatus.CREATED); // Returns a 201 CREATED status with the new task.
    }

    @PutMapping("/{id}") // Maps HTTP PUT requests to "/api/tasks/{id}".
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        // Attempts to update the task. If successful, returns 200 OK; otherwise, 404 Not Found.
        return taskService.updateTask(id, taskRequestDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}") // Maps HTTP DELETE requests to "/api/tasks/{id}".
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        // Attempts to delete the task. If successful, returns 204 No Content; otherwise, 404 Not Found.
        if (taskService.deleteTask(id)) {
            return ResponseEntity.noContent().build(); // 204 No Content indicates successful deletion with no body.
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found if the task ID doesn't exist.
        }
    }
}