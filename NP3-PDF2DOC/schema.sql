-- Bước 1: Tạo Database (nếu chưa có)
CREATE DATABASE IF NOT EXISTS async_pdf_project CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE async_pdf_project;

-- Bước 2: Tạo bảng users
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bước 3: Tạo bảng processing_tasks (Bảng Quản lý Hàng đợi/Trạng thái)
CREATE TABLE processing_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    result_summary TEXT,
    result_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Khóa ngoại liên kết với bảng users
    FOREIGN KEY (user_id) REFERENCES users(id)
);