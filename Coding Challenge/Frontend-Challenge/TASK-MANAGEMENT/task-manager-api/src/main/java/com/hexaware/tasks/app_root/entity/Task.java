package com.hexaware.tasks.app_root.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
// @AllArgsConstructor // Remove this and create a custom constructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // --- NEW: Add Many-to-One relationship to User ---
    @ManyToOne(fetch = FetchType.LAZY) // Many tasks can belong to one user
    @JoinColumn(name = "user_id", nullable = false) // This will be the foreign key column in the 'tasks' table
    private User user; // The user who owns this task
    // --- END NEW ---

    // Custom constructor to include 'user' when creating a new Task
    public Task(Long id, String title, String description, LocalDate dueDate, Priority priority, Status status,
            User user) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.user = user; // Initialize the user
    }

    // You might also want a constructor for new tasks without an ID initially
    public Task(String title, String description, LocalDate dueDate, Priority priority, Status status, User user) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.user = user;
    }
}