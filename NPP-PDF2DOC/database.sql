-- Bước 1: Tạo Database (nếu chưa có)
CREATE DATABASE IF NOT EXISTS nppp2d_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE nppp2d_db;

-- Bước 2: Tạo bảng users (loại bỏ created_at)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100)
);

-- Bước 2.1: Thêm bản ghi test ban đầu (dùng cho mục đích thử nghiệm)
-- LƯU Ý: Mật khẩu tài khoản test ở đây là 123456;
INSERT INTO users (username, password, email) VALUES ('testuser', '$2a$12$iO7gjYSZINki5VJArqlFAuP.q9IT0nbVBfHW0Hb/f6jCjIGrkn3gi', 'test@example.com');

-- Bước 3: Tạo bảng processing_tasks (Bảng Quản lý Hàng đợi/Trạng thái)
-- Thay vì xóa, đổi tên cột `created_at` thành `date` để lưu thời điểm tạo
CREATE TABLE processing_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    result_summary TEXT,
    result_path VARCHAR(255),
    `date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Khóa ngoại liên kết với bảng users
    FOREIGN KEY (user_id) REFERENCES users(id)
);
