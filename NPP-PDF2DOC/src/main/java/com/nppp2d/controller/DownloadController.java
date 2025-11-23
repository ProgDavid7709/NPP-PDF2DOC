package com.nppp2d.controller;

import com.nppp2d.model.bean.Task;
import com.nppp2d.model.dao.TaskDAO;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/download")
public class DownloadController extends HttpServlet {

    private TaskDAO taskDAO;

    public void init() {
        taskDAO = new TaskDAO();
    }

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        // --- 1. KIỂM TRA ĐĂNG NHẬP (Giữ nguyên) ---
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Bạn cần đăng nhập để tải file."
            );
            return;
        }
        int currentUserId = (int) session.getAttribute("userId");
        //    	int currentUserId = 1;

        // --- 2. LẤY TASK ID TỪ URL (Giữ nguyên) ---
        int taskId;
        try {
            taskId = Integer.parseInt(request.getParameter("taskId"));
        } catch (NumberFormatException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Task ID không hợp lệ."
            );
            return;
        }

        // SỬA LỖI TRY-CATCH: Bắt cả SQLException
        try {
            // --- 3. KIỂM TRA QUYỀN SỞ HỮU (Giữ nguyên) ---
            Task task = taskDAO.getTaskById(taskId);

            if (task == null || task.getUserId() != currentUserId) {
                response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập file này."
                );
                return;
            }

            if (!"COMPLETED".equals(task.getStatus())) {
                response.sendError(
                    HttpServletResponse.SC_CONFLICT,
                    "File này chưa được xử lý xong."
                );
                return;
            }

            // --- 4. STREAM FILE VỀ CLIENT ---
            File fileToDownload = new File(task.getResultPath());
            if (!fileToDownload.exists()) {
                response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "File kết quả không còn tồn tại trên server."
                );
                return;
            }

            // --- A. Cấu hình HTTP Headers (THAY ĐỔI Ở ĐÂY) ---

            // Serve .docx and set safe headers
            response.setContentType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );

            response.setContentLengthLong(fileToDownload.length());

            // Use the actual filename of the stored result file so the client receives the original result name
            String filename = fileToDownload.getName();
            // URLEncoder encodes spaces as '+', convert those to %20 for wider Content-Disposition compatibility
            String encodedFilename = URLEncoder.encode(
                filename,
                StandardCharsets.UTF_8.toString()
            ).replaceAll("\\+", "%20");

            response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" +
                    filename +
                    "\"; filename*=UTF-8''" +
                    encodedFilename
            );
            // Prevent browsers from sniffing content type
            response.setHeader("X-Content-Type-Options", "nosniff");

            // --- B. Stream file using NIO for efficiency ---
            try {
                Files.copy(fileToDownload.toPath(), response.getOutputStream());
                response.getOutputStream().flush();
            } catch (IOException e) {
                throw e;
            }
        } catch (IOException e) {
            System.err.println("Lỗi I/O khi stream file: " + e.getMessage());
        }
    }
}
