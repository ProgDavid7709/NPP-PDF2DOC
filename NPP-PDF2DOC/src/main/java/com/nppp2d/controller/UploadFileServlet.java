package com.nppp2d.controller;

import com.nppp2d.model.bean.Task;
import com.nppp2d.model.bo.TaskQueueBO;
import com.nppp2d.model.dao.TaskDAO;
import com.nppp2d.model.worker.FileConvertWorker;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig; // QUAN TRỌNG CHO UPLOAD
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/process")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 100, // 100MB
    maxRequestSize = 1024 * 1024 * 150
) // 150MB
public class UploadFileServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(
        UploadFileServlet.class
    );

    private String uploadDir;
    private TaskDAO taskDAO;

    public void init() {
        taskDAO = new TaskDAO();

        // Load upload directory from application.properties (optional) or use default
        Properties props = new Properties();
        try (
            InputStream in = getClass()
                .getClassLoader()
                .getResourceAsStream("application.properties")
        ) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // Ignore and use default
        }
        /*
         * Base upload/result root. We'll create a structured layout under this:
         *
         *  <uploadRoot>/
         *    nppp2d_saves/
         *      Queue/
         *        <clientIp>.User<userId>/
         *      Results/
         *        <clientIp>.User<userId>/
         *
         * The servlet will store incoming PDFs into the Queue/<client>/ directory.
         * The worker may later write results into the Results/<client>/ directory.
         *
         * The property `app.upload-dir` continues to provide the filesystem root.
         */
        uploadDir = props.getProperty("app.upload-dir", "E:/nppp2d_saves/");
        // Ensure base structure exists: Queue/ and Results/ under the configured root
        try {
            Path root = Paths.get(uploadDir);
            Path queue = root.resolve("Queue");
            Path results = root.resolve("Results");
            Files.createDirectories(queue);
            Files.createDirectories(results);
            // keep uploadDir pointing at the configured root so other code can resolve subpaths
            uploadDir = root.toString();
        } catch (IOException e) {
            throw new RuntimeException(
                "Unable to create upload directory structure under: " +
                    uploadDir,
                e
            );
        }
    }

    /**
     * PHẦN THÊM VÀO: Xử lý GET request (Sửa lỗi 405)
     * Nhiệm vụ của GET là HIỂN THỊ trang upload file (upload.jsp).
     */
    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Chuyển tiếp đến trang JSP để hiển thị form process
        request
            .getRequestDispatcher("/WEB-INF/jsp/process.jsp")
            .forward(request, response);
    }

    /**
     * PHẦN CÓ SẴN: Xử lý POST request
     * Nhiệm vụ của POST là nhận file và đưa vào hàng đợi.
     */
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        int currentId = (int) session.getAttribute("userId");
        response.setContentType("application/json;charset=UTF-8");
        // upload directory is prepared during servlet init()

        try {
            // Support multiple uploaded parts named 'pdfFile'
            java.util.Collection<Part> parts = request.getParts();
            java.util.List<Integer> createdTaskIds =
                new java.util.ArrayList<>();
            java.util.List<String> errors = new java.util.ArrayList<>();

            // Prepare user directories (same as previous single-file flow)
            String username = (String) session.getAttribute("username");
            if (username == null || username.trim().isEmpty()) {
                username = "User" + currentId;
            }
            String userDirName = username.replaceAll("[^A-Za-z0-9._-]", "_");
            Path queueDir = Paths.get(uploadDir)
                .resolve("Queue")
                .resolve(userDirName);
            Path resultsDir = Paths.get(uploadDir)
                .resolve("Results")
                .resolve(userDirName);
            try {
                Files.createDirectories(queueDir);
                Files.createDirectories(resultsDir);
            } catch (IOException e) {
                throw new ServletException(
                    "Unable to create user directories: " +
                        queueDir +
                        " or " +
                        resultsDir,
                    e
                );
            }

            boolean anyAccepted = false;

            for (Part p : parts) {
                if (p == null) continue;
                // Only consider parts with the expected field name
                if (!"pdfFile".equals(p.getName())) continue;
                if (p.getSize() == 0) continue;

                // Sanitize submitted filename and restrict to PDFs
                String submittedFileName = Paths.get(p.getSubmittedFileName())
                    .getFileName()
                    .toString();
                String lower = submittedFileName.toLowerCase();
                if (!lower.endsWith(".pdf")) {
                    errors.add(submittedFileName + ": Chỉ chấp nhận file PDF.");
                    continue;
                }

                anyAccepted = true;

                // Persist with timestamp prefix to avoid collisions
                String targetFileName =
                    System.currentTimeMillis() + "_" + submittedFileName;
                Path targetPath = queueDir.resolve(targetFileName);

                // Save uploaded stream to disk
                try (InputStream in = p.getInputStream()) {
                    Files.copy(in, targetPath);
                } catch (IOException ioe) {
                    errors.add(submittedFileName + ": Lỗi khi lưu file.");
                    continue;
                }

                String storedPath = targetPath.toString();

                // Create DB Task record
                Task newTask = new Task(
                    currentId,
                    submittedFileName,
                    storedPath
                );
                int TaskId = -1;
                try {
                    TaskId = taskDAO.createTask(newTask);
                } catch (SQLException sqle) {
                    errors.add(submittedFileName + ": Lỗi DB khi tạo Task.");
                    // try to cleanup the stored file
                    try {
                        Files.deleteIfExists(targetPath);
                    } catch (IOException ignored) {}
                    continue;
                }

                logger.info(
                    "Created Task record id={} userId={} file={} storedPath={}",
                    TaskId,
                    currentId,
                    submittedFileName,
                    storedPath
                );
                createdTaskIds.add(TaskId);

                // Submit worker for this file
                FileConvertWorker worker = new FileConvertWorker(
                    TaskId,
                    storedPath,
                    taskDAO
                );
                try {
                    logger.info(
                        "Submitting worker for Task ID {} (file={})",
                        TaskId,
                        storedPath
                    );
                    TaskQueueBO.submit(worker);
                    logger.info(
                        "Worker successfully submitted for Task ID {}",
                        TaskId
                    );
                } catch (Exception ex) {
                    logger.error(
                        "Failed to submit worker for Task ID {}: {}",
                        TaskId,
                        ex.getMessage(),
                        ex
                    );
                    try {
                        taskDAO.updateTaskStatus(TaskId, "FAILED");
                    } catch (SQLException ignored) {}
                    errors.add(
                        submittedFileName + ": Không thể bắt đầu xử lý file."
                    );
                }
            }

            if (!anyAccepted) {
                // No valid files were uploaded
                request.setAttribute(
                    "error",
                    "Không có file PDF hợp lệ được tải lên."
                );
                request
                    .getRequestDispatcher("/WEB-INF/jsp/process.jsp")
                    .forward(request, response);
                return;
            }

            // Return JSON summary with created task IDs and any per-file errors
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"status\":\"queued\",");
            sb.append("\"taskIds\":[");
            for (int i = 0; i < createdTaskIds.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(createdTaskIds.get(i));
            }
            sb.append("],");
            sb.append(
                "\"message\":\"Các file đã được đưa vào hàng đợi. Vui lòng vào Dashboard để kiểm tra.\""
            );
            if (!errors.isEmpty()) {
                sb.append(",\"errors\":[");
                for (int i = 0; i < errors.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb
                        .append("\"")
                        .append(errors.get(i).replace("\"", "\\\""))
                        .append("\"");
                }
                sb.append("]");
            }
            sb.append("}");
            response.getWriter().write(sb.toString());
            response.getWriter().flush();
        } catch (Exception e) {
            // LOG LỖI ĐỂ DEBUG: Quan trọng để biết tại sao file upload thất bại
            logger.error("Upload error", e);
            e.printStackTrace();

            // TRẢ VỀ JSON LỖI thay vì forward
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Trả về 500

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"status\":\"error\",");
            // Escape dấu ngoặc kép trong message để tránh lỗi JSON
            String msg = e.getMessage() != null
                ? e.getMessage().replace("\"", "\\\"")
                : "Unknown error";
            sb.append("\"message\":\"Lỗi Server: " + msg + "\"");
            sb.append("}");

            response.getWriter().write(sb.toString());
            response.getWriter().flush();
        }
    }
}
