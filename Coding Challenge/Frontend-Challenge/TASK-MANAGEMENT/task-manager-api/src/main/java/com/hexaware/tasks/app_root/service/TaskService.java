package com.hexaware.tasks.app_root.service; // CORRECTED package declaration

// package com.hexaware.tasks.app_root.service;
// TaskService.java

import com.hexaware.tasks.app_root.dto.TaskRequestDTO;
import com.hexaware.tasks.app_root.dto.TaskResponseDTO;
import com.hexaware.tasks.app_root.entity.Task;
import com.hexaware.tasks.app_root.entity.Priority;
import com.hexaware.tasks.app_root.entity.Status;
import com.hexaware.tasks.app_root.entity.User; // Import User
import com.hexaware.tasks.app_root.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // --- MODIFIED: Get all tasks for a specific user ---
    public List<TaskResponseDTO> getAllTasks(User user) {
        List<Task> tasks = taskRepository.findByUser(user); // Filter by user
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // --- MODIFIED: Get task by ID, ensuring it belongs to the user ---
    public Optional<TaskResponseDTO> getTaskById(Long id, User user) {
        Optional<Task> task = taskRepository.findByIdAndUser(id, user); // Filter by ID AND user
        return task.map(this::convertToDto);
    }

    // --- MODIFIED: Add task, assigning it to the user ---
    public TaskResponseDTO addTask(TaskRequestDTO taskRequestDTO, User user) {
        Task task = convertToEntity(taskRequestDTO);
        task.setUser(user); // Assign the current user to the task
        Task savedTask = taskRepository.save(task);
        return convertToDto(savedTask);
    }

    // --- MODIFIED: Update task, ensuring it belongs to the user ---
    public Optional<TaskResponseDTO> updateTask(Long id, TaskRequestDTO taskRequestDTO, User user) {
        // Find by ID and user to prevent updating tasks of other users
        return taskRepository.findByIdAndUser(id, user).map(existingTask -> {
            existingTask.setTitle(taskRequestDTO.getTitle());
            existingTask.setDescription(taskRequestDTO.getDescription());
            existingTask.setDueDate(taskRequestDTO.getDueDate());
            existingTask.setPriority(taskRequestDTO.getPriority());
            existingTask.setStatus(taskRequestDTO.getStatus());
            Task updatedTask = taskRepository.save(existingTask);
            return convertToDto(updatedTask);
        });
    }

    // --- MODIFIED: Delete task, ensuring it belongs to the user ---
    public boolean deleteTask(Long id, User user) {
        // Check if task exists AND belongs to the user before deleting
        if (taskRepository.existsByIdAndUser(id, user)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false; // Task not found or not owned by the user
    }

    private TaskResponseDTO convertToDto(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getPriority(),
                task.getStatus());
    }

    // This method is now used by addTask, so it no longer needs to assign the user
    private Task convertToEntity(TaskRequestDTO taskRequestDTO) {
        return new Task(
                null, // ID is null for new entities
                taskRequestDTO.getTitle(),
                taskRequestDTO.getDescription(),
                taskRequestDTO.getDueDate(),
                taskRequestDTO.getPriority(),
                taskRequestDTO.getStatus(),
                null // User will be set separately in the service/controller
        );
    }
}