package com.laptrinhmang.asyncapp.model.worker;

import com.laptrinhmang.asyncapp.model.bean.ProcessingTask;
import com.laptrinhmang.asyncapp.model.dao.TaskDAO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

// === CÁC IMPORT MỚI CHO .DOC ===
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import java.io.FileOutputStream; 
// ===============================

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List; // Import dự phòng

public class PDFProcessingWorker implements Runnable {
    
    private final int taskId;
    private final String pdfFilePath;
    private final TaskDAO taskDAO;
    private static final String RESULT_DIR = "E:/async_results/";

    public PDFProcessingWorker(int taskId, String pdfFilePath, TaskDAO taskDAO) {
        this.taskId = taskId;
        this.pdfFilePath = pdfFilePath;
        this.taskDAO = taskDAO;
    }

    @Override
    public void run() {
        String resultPath = null;
        try {
            // 1. Cập nhật trạng thái "PROCESSING"
            taskDAO.updateTaskStatus(taskId, "PROCESSING");
            System.out.println("Task ID " + taskId + ": Bắt đầu xử lý file...");
            
            // 2. Trích xuất Text từ PDF (Giữ nguyên)
            File pdfFile = new File(pdfFilePath);
            String extractedText = "";
            try (PDDocument document = PDDocument.load(pdfFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                extractedText = stripper.getText(document);
            }

            int wordCount = 0;
            if (extractedText != null && !extractedText.trim().isEmpty()) {
                wordCount = extractedText.split("\\s+").length;
            }

            // === 3. THAY ĐỔI LOGIC: GHI FILE .DOC ===
            resultPath = RESULT_DIR + "result_" + taskId + ".doc"; // Đổi đuôi file
            File resultFile = new File(resultPath);
            resultFile.getParentFile().mkdirs();
            InputStream templateStream = getClass().getClassLoader().getResourceAsStream("blank_template.doc");
            if (templateStream == null) {
                throw new IOException("Không tìm thấy file mẫu 'blank_template.doc' trong resources.");
            }
            // 3a. Tạo một tài liệu Word (.doc) rỗng
            try (HWPFDocument doc = new HWPFDocument(templateStream);
                 FileOutputStream out = new FileOutputStream(resultFile)) {
                Range range = doc.getRange();
                String wordText = extractedText.replaceAll("\\n", "\r");
                range.insertAfter(wordText);
                doc.write(out);
            }

            String summary = "Hoàn thành. Số từ: " + wordCount;
            taskDAO.updateTaskResult(taskId, summary, resultPath); 
            taskDAO.updateTaskStatus(taskId, "COMPLETED");
            File originalPdfFile = new File(pdfFilePath);
            if (originalPdfFile.exists()) {
                originalPdfFile.delete();
                System.out.println("Task ID " + taskId + ": Đã dọn dẹp file PDF gốc.");
            }
            System.out.println("Task ID " + taskId + ": Xử lý thành công. Số từ: " + wordCount);

        } catch (SQLException eSQL) {
            System.err.println("Lỗi DB Worker Task " + taskId + ": " + eSQL.getMessage());
            try {
                taskDAO.updateTaskStatus(taskId, "FAILED");
            } catch (SQLException ignored) { }
        } catch (Exception e) { 
            System.err.println("Lỗi Xử lý File Worker Task " + taskId + ": " + e.getMessage());
             try {
                taskDAO.updateTaskStatus(taskId, "FAILED");
            } catch (SQLException ignored) { }
        }
    }

    public static void main(String[] args) {
        TaskDAO taskDAO = new TaskDAO();
        int userId = 1; 
        String validFilePath = "E:/test.pdf"; 
        String invalidFilePath = "/path/to/non_existent_file.pdf";
        try {
            System.out.println("--- TEST 1: KỊCH BẢN THÀNH CÔNG (.DOC) ---");
            
            ProcessingTask p1 = new ProcessingTask(userId, "test.pdf", validFilePath);
            int taskId1 = taskDAO.createTask(p1);
            System.out.println("   -> Task PENDING ID: " + taskId1);

            PDFProcessingWorker worker1 = new PDFProcessingWorker(taskId1, validFilePath, taskDAO);
            worker1.run(); 
            
            System.out.println("   -> KẾT QUẢ DB: Kiểm tra bảng processing_tasks. Status phải là COMPLETED.");
            
        } catch (SQLException e) {
            System.err.println("Lỗi SQL trong quá trình TEST: " + e.getMessage());
        }
    }
}