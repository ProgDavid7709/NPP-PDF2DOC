package com.laptrinhmang.asyncapp.model.dao;
import java.sql.ResultSet;
import com.laptrinhmang.asyncapp.model.bean.ProcessingTask;
import com.laptrinhmang.asyncapp.util.DBConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public int createTask(ProcessingTask task) throws SQLException {
        String sql = "INSERT INTO processing_tasks (user_id, file_name, file_path, status) VALUES (?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, task.getUserId());
            ps.setString(2, task.getFileName());
            ps.setString(3, task.getFilePath());
            ps.setString(4, "PENDING"); 
            
            ps.executeUpdate();
            
            // Lấy ID vừa được sinh ra
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                }
            }
        }
        return generatedId;
    }
    public void updateTaskStatus(int taskId, String status) throws SQLException {
        String sql = "UPDATE processing_tasks SET status = ? WHERE id = ?";
        
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        }
    }

    public List<ProcessingTask> getTasksByUserId(int userId) throws SQLException {
        List<ProcessingTask> taskList = new ArrayList<>();
        String sql = "SELECT * FROM processing_tasks WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProcessingTask task = new ProcessingTask();
                    task.setId(rs.getInt("id"));
                    task.setFileName(rs.getString("file_name"));
                    task.setStatus(rs.getString("status"));
                    task.setResultSummary(rs.getString("result_summary"));
                    task.setResultPath(rs.getString("result_path"));
                    task.setCreatedAt(rs.getTimestamp("created_at"));
                    taskList.add(task);
                }
            }
        }
        return taskList;
    }
    public ProcessingTask getTaskById(int taskId) {
        // 1. Câu lệnh SQL
        String sql = "SELECT * FROM processing_tasks WHERE id = ?";
        ProcessingTask task = null; 
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    task = new ProcessingTask();
                    task.setId(rs.getInt("id"));
                    task.setUserId(rs.getInt("user_id"));
                    task.setFileName(rs.getString("file_name"));
                    task.setFilePath(rs.getString("file_path"));
                    task.setStatus(rs.getString("status"));
                    task.setResultSummary(rs.getString("result_summary"));
                    task.setResultPath(rs.getString("result_path"));
                    task.setCreatedAt(rs.getTimestamp("created_at"));
                }
               
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Lỗi khi lấy Task bằng ID (" + taskId + "): " + e.getMessage());
        }
        
        // 8. Trả về kết quả
        return task;
    }
    
    public void updateTaskResult(int taskId, String summary, String resultPath) { 
    	String sql = "UPDATE processing_tasks set result_summary = ?,result_path = ? WHERE id = ?";
    	try (Connection conn = DBConnectionUtil.getConnection();
    			PreparedStatement ps = conn.prepareStatement(sql)){
    		ps.setString(1,summary);
    		ps.setString(2, resultPath);
    		ps.setInt(3,taskId);
    		
    		ps.executeUpdate();
    	}catch(SQLException e) {
    		System.out.println("cập nhật lỗi " + e.getMessage());
    	}
    }
    public static void main(String[] args) {
        TaskDAO taskDAO = new TaskDAO();
        int userIdToTest = 1; 
        
        System.out.println("--- BẮT ĐẦU KIỂM TRA TASKDAO ---");

        try {
            // --- 1. Thử Test hàm createTask ---
            System.out.println("Đang test createTask()...");
            ProcessingTask newTask = new ProcessingTask();
            newTask.setUserId(userIdToTest);
            newTask.setFileName("test_file.pdf");
            newTask.setFilePath("/uploads/test_file.pdf");
            
            int newTaskId = taskDAO.createTask(newTask);
            if (newTaskId > 0) {
                System.out.println("Thành công: Đã tạo Task mới với ID = " + newTaskId);
            } else {
                System.out.println("Thất bại: Không tạo được Task.");
                return; // Dừng test nếu không tạo được
            }
            System.out.println("\nĐang test updateTaskStatus()...");
            taskDAO.updateTaskStatus(newTaskId, "PROCESSING");
            System.out.println("Thành công: Đã cập nhật status cho Task ID " + newTaskId);

            // --- 3. Thử Test hàm updateTaskResult ---
            System.out.println("\nĐang test updateTaskResult()...");
            taskDAO.updateTaskResult(newTaskId, "1234", "/results/test_file.txt");
            System.out.println("Thành công: Đã cập nhật kết quả cho Task ID " + newTaskId);

            // --- 4. Thử Test hàm getTasksByUserId ---
            System.out.println("\nĐang test getTasksByUserId()...");
            List<ProcessingTask> tasks = taskDAO.getTasksByUserId(userIdToTest);
            
            if (tasks.isEmpty()) {
                System.out.println("Lỗi: Không tìm thấy Task nào cho user " + userIdToTest);
            } else {
                System.out.println("Thành công: Tìm thấy " + tasks.size() + " Task cho user " + userIdToTest);

                for (int i = 0;i < tasks.size();i++) {
                	ProcessingTask firstTask = tasks.get(i);
                	System.out.println(" ----------------------------");
                    System.out.println("  -> Task ID: " + firstTask.getId());
                    System.out.println("  -> File Name: " + firstTask.getFileName());
                    System.out.println("  -> Status: " + firstTask.getStatus());
                    System.out.println("  -> Summary: " + firstTask.getResultSummary());
                }
                
            }

        } catch (SQLException e) {
            System.out.println("\n--- LỖI SQL ---");
            System.out.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n--- KIỂM TRA HOÀN TẤT ---");
    }
 }
