package com.nppp2d.model.dao;

import com.nppp2d.model.bean.Task;
import com.nppp2d.util.DBConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public int createTask(Task task) throws SQLException {
        String sql =
            "INSERT INTO processing_tasks (user_id, file_name, file_path, status) VALUES (?, ?, ?, ?)";
        int generatedId = -1;

        try (
            Connection conn = DBConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
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

    public void updateTaskStatus(int taskId, String status)
        throws SQLException {
        String sql = "UPDATE processing_tasks SET status = ? WHERE id = ?";

        try (
            Connection conn = DBConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, status);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        }
    }

    public List<Task> getTasksByUserId(int userId) throws SQLException {
        List<Task> taskList = new ArrayList<>();
        String sql =
            "SELECT * FROM processing_tasks WHERE user_id = ? ORDER BY `date` DESC";

        try (
            Connection conn = DBConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("id"));
                    task.setUserId(rs.getInt("user_id"));
                    task.setFileName(rs.getString("file_name"));
                    task.setFilePath(rs.getString("file_path"));
                    task.setStatus(rs.getString("status"));
                    task.setResultSummary(rs.getString("result_summary"));
                    task.setResultPath(rs.getString("result_path"));
                    task.setDate(rs.getTimestamp("date"));
                    taskList.add(task);
                }
            }
        }
        return taskList;
    }

    public Task getTaskById(int taskId) {
        // 1. Câu lệnh SQL
        String sql = "SELECT * FROM processing_tasks WHERE id = ?";
        Task task = null;
        try (
            Connection conn = DBConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, taskId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    task = new Task();
                    task.setId(rs.getInt("id"));
                    task.setUserId(rs.getInt("user_id"));
                    task.setFileName(rs.getString("file_name"));
                    task.setFilePath(rs.getString("file_path"));
                    task.setStatus(rs.getString("status"));
                    task.setResultSummary(rs.getString("result_summary"));
                    task.setResultPath(rs.getString("result_path"));
                    task.setDate(rs.getTimestamp("date"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(
                "Lỗi khi lấy Task bằng ID (" + taskId + "): " + e.getMessage()
            );
        }

        // 8. Trả về kết quả
        return task;
    }

    public void updateTaskResult(
        int taskId,
        String summary,
        String resultPath
    ) {
        String sql =
            "UPDATE processing_tasks set result_summary = ?,result_path = ? WHERE id = ?";
        try (
            Connection conn = DBConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, summary);
            ps.setString(2, resultPath);
            ps.setInt(3, taskId);

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("cập nhật lỗi " + e.getMessage());
        }
    }

    // main() test removed — testing logic moved to proper unit tests (no main in production classes)
}
