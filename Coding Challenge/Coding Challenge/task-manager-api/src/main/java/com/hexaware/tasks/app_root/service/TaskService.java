package com.hexaware.tasks.app_root.service; // CORRECTED package declaration

import com.hexaware.tasks.app_root.dto.TaskRequestDTO; // CORRECTED import
import com.hexaware.tasks.app_root.dto.TaskResponseDTO; // CORRECTED import
import com.hexaware.tasks.app_root.entity.Task; // CORRECTED import
import com.hexaware.tasks.app_root.entity.Priority; // CORRECTED import
import com.hexaware.tasks.app_root.entity.Status; // CORRECTED import
import com.hexaware.tasks.app_root.repository.TaskRepository; // CORRECTED import
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

    public List<TaskResponseDTO> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<TaskResponseDTO> getTaskById(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return task.map(this::convertToDto);
    }

    public TaskResponseDTO addTask(TaskRequestDTO taskRequestDTO) {
        Task task = convertToEntity(taskRequestDTO);
        Task savedTask = taskRepository.save(task);
        return convertToDto(savedTask);
    }

    public Optional<TaskResponseDTO> updateTask(Long id, TaskRequestDTO taskRequestDTO) {
        return taskRepository.findById(id).map(existingTask -> {
            existingTask.setTitle(taskRequestDTO.getTitle());
            existingTask.setDescription(taskRequestDTO.getDescription());
            existingTask.setDueDate(taskRequestDTO.getDueDate());
            existingTask.setPriority(taskRequestDTO.getPriority());
            existingTask.setStatus(taskRequestDTO.getStatus());
            Task updatedTask = taskRepository.save(existingTask);
            return convertToDto(updatedTask);
        });
    }

    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private TaskResponseDTO convertToDto(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getPriority(),
                task.getStatus()
        );
    }

    private Task convertToEntity(TaskRequestDTO taskRequestDTO) {
        return new Task(
                null,
                taskRequestDTO.getTitle(),
                taskRequestDTO.getDescription(),
                taskRequestDTO.getDueDate(),
                taskRequestDTO.getPriority(),
                taskRequestDTO.getStatus()
        );
    }
}