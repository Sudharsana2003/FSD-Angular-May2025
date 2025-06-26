package com.hexaware.tasks.app_root.repository;
import com.hexaware.tasks.app_root.entity.Task; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}