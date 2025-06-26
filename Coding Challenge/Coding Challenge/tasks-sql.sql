CREATE DATABASE taskdb;

USE taskdb;

CREATE TABLE tasks (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     title VARCHAR(255) NOT NULL,
     description TEXT,
     due_date DATE,
     priority VARCHAR(50) NOT NULL,
     status VARCHAR(50) NOT NULL
     );
     
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE, 
    password VARCHAR(255) NOT NULL,       
    roles VARCHAR(255) NOT NULL            
);