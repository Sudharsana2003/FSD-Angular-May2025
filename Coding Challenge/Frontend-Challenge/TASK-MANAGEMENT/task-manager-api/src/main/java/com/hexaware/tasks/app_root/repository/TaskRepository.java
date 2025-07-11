package com.hexaware.tasks.app_root.repository;

import com.hexaware.tasks.app_root.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hexaware.tasks.app_root.entity.User; // Import the User entity

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Find all tasks associated with a specific user
    List<Task> findByUser(User user);

    // Find a task by its ID AND ensuring it belongs to a specific user
    Optional<Task> findByIdAndUser(Long id, User user);

    // Check if a task exists by ID AND ensuring it belongs to a specific user
    boolean existsByIdAndUser(Long id, User user);
}