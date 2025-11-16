package com.laptrinhmang.asyncapp.controller;

import com.laptrinhmang.asyncapp.model.bean.ProcessingTask;
import com.laptrinhmang.asyncapp.model.dao.TaskDAO;
import com.laptrinhmang.asyncapp.model.service.TaskQueueService;
import com.laptrinhmang.asyncapp.model.worker.PDFProcessingWorker;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig; // QUAN TRỌNG CHO UPLOAD
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
                 maxFileSize = 1024 * 1024 * 100,  // 100MB
                 maxRequestSize = 1024 * 1024 * 150) // 150MB
public class UploadFileServlet extends HttpServlet {
	
	private static final String UPLOAD_DIR = "E:/async_results/";
	private TaskDAO taskDAO;
	
	public void init() {
		taskDAO = new TaskDAO();
	}

    /**
     * PHẦN THÊM VÀO: Xử lý GET request (Sửa lỗi 405)
     * Nhiệm vụ của GET là HIỂN THỊ trang upload file (upload.jsp).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Chuyển tiếp đến trang JSP để hiển thị form upload
        request.getRequestDispatcher("/WEB-INF/jsp/upload.jsp").forward(request, response);
    }

    /**
     * PHẦN CÓ SẴN: Xử lý POST request
     * Nhiệm vụ của POST là nhận file và đưa vào hàng đợi.
     */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			response.sendRedirect(request.getContextPath()+"/login");
			return;
		}
		int currentId = (int)session.getAttribute("userId");
		
		File uploadDir = new File(UPLOAD_DIR);
		if (!uploadDir.exists()) {
			uploadDir.mkdir();
		}
		
		try {
			// Tên "pdfFile" này phải khớp với 'name' trong form của upload.jsp
			Part filePart = request.getPart("pdfFile"); 
			String fileName = filePart.getSubmittedFileName();
			String tempFilePath = UPLOAD_DIR + System.currentTimeMillis() + "_" + fileName;
			
			// 1. Lưu file tạm
			filePart.write(tempFilePath);
			
			// 2. Tạo Task trong DB (status: PENDING)
			ProcessingTask newTask = new ProcessingTask(currentId, fileName, tempFilePath);
			int TaskId = taskDAO.createTask(newTask); // Gọi Người 1
			
			// 3. Tạo Worker và đưa vào hàng đợi (Queue)
			PDFProcessingWorker worker = new PDFProcessingWorker(TaskId, tempFilePath, taskDAO);
			TaskQueueService.submit(worker); // Gọi Người 2
			
			// 4. Trả về trang status ngay lập tức
			response.sendRedirect(request.getContextPath() + "/status");
			
		} catch (SQLException e) {
            request.setAttribute("error", "Lỗi DB khi tạo Task.");
            // Sửa lỗi: Chuyển về trang upload để báo lỗi
            request.getRequestDispatcher("/WEB-INF/jsp/upload.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi xử lý file upload: " + e.getMessage());
            // Sửa lỗi: Chuyển về trang upload để báo lỗi
            request.getRequestDispatcher("/WEB-INF/jsp/upload.jsp").forward(request, response);
        }
	}
}

