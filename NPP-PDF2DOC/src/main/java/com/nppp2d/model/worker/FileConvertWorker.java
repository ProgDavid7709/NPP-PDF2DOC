package com.nppp2d.model.worker;

import com.nppp2d.model.bean.Task;
import com.nppp2d.model.dao.TaskDAO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Improved FileConvertWorker:
 * - Extracts text on a per-page basis and inserts page breaks into the DOCX
 * - Extracts images embedded in each PDF page and inserts them into the DOCX
 * - Keeps previous behavior of updating task status/results and picking Result directory
 *
 * Notes:
 * - This implementation uses PDFBox to read pages and images and Apache POI XWPF to write DOCX.
 * - Images are written as PNGs to the docx. Large images are scaled down to a reasonable width.
 */
public class FileConvertWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(
        FileConvertWorker.class
    );

    private final int taskId;
    private final String pdfFilePath;
    private final TaskDAO taskDAO;
    // Root where results and queue live. Per requirement use E:/nppp2d_saves/
    private static final String RESULT_DIR = "E:/nppp2d_saves/";
    // maximum image width in pixels when embedding into docx (to avoid extremely large images)
    private static final int MAX_IMAGE_WIDTH_PX = 800;

    public FileConvertWorker(int taskId, String pdfFilePath, TaskDAO taskDAO) {
        this.taskId = taskId;
        this.pdfFilePath = pdfFilePath;
        this.taskDAO = taskDAO;
    }

    @Override
    public void run() {
        String resultPath = null;
        try {
            // 1. Update status
            taskDAO.updateTaskStatus(taskId, "PROCESSING");
            logger.info("Task ID {}: Bắt đầu xử lý file...", taskId);

            File pdfFile = new File(pdfFilePath);
            if (!pdfFile.exists()) {
                logger.error(
                    "Task ID {}: File PDF không tồn tại: {}",
                    taskId,
                    pdfFilePath
                );
                taskDAO.updateTaskStatus(taskId, "FAILED");
                return;
            }

            // We'll accumulate full text for summary/word count, but extract per-page to improve structure.
            StringBuilder fullTextBuilder = new StringBuilder();

            // Prepare DOCX document
            Path finalResultPath;
            XWPFDocument docx = new XWPFDocument();
            try (PDDocument pdfDoc = PDDocument.load(pdfFile)) {
                int numPages = pdfDoc.getNumberOfPages();
                PDFTextStripper stripper = new PDFTextStripper();

                for (int i = 1; i <= numPages; i++) {
                    // Extract text for this page only
                    stripper.setStartPage(i);
                    stripper.setEndPage(i);
                    String pageText = stripper.getText(pdfDoc);
                    if (pageText == null) pageText = "";
                    fullTextBuilder.append(pageText);
                    if (!pageText.endsWith(System.lineSeparator())) {
                        fullTextBuilder.append(System.lineSeparator());
                    }

                    // Create a paragraph for the page text
                    XWPFParagraph p = docx.createParagraph();
                    XWPFRun r = p.createRun();
                    // To preserve long text and line breaks, split lines and add runs with breaks.
                    String[] lines = pageText.split("\\r?\\n");
                    for (int li = 0; li < lines.length; li++) {
                        String line = lines[li];
                        if (line == null) line = "";
                        r.setText(line);
                        if (li < lines.length - 1) {
                            r.addBreak();
                        }
                    }

                    // Extract images for this page and insert them after the text
                    PDPage pdPage = pdfDoc.getPage(i - 1);
                    PDResources resources = pdPage.getResources();
                    if (resources != null) {
                        Iterable<COSName> xObjectNames =
                            resources.getXObjectNames();
                        Iterator<COSName> it = xObjectNames.iterator();
                        while (it.hasNext()) {
                            COSName xObjectName = it.next();
                            try {
                                PDXObject xObj = resources.getXObject(
                                    xObjectName
                                );
                                if (xObj instanceof PDImageXObject) {
                                    PDImageXObject imageXObject =
                                        (PDImageXObject) xObj;
                                    BufferedImage bimg =
                                        imageXObject.getImage();
                                    if (bimg == null) continue;

                                    // scale if too wide
                                    int imgW = bimg.getWidth();
                                    int imgH = bimg.getHeight();
                                    double scale = 1.0;
                                    if (imgW > MAX_IMAGE_WIDTH_PX) {
                                        scale =
                                            (double) MAX_IMAGE_WIDTH_PX /
                                            (double) imgW;
                                    }
                                    int targetW = (int) Math.max(
                                        1,
                                        Math.round(imgW * scale)
                                    );
                                    int targetH = (int) Math.max(
                                        1,
                                        Math.round(imgH * scale)
                                    );

                                    // convert BufferedImage to PNG bytes
                                    ByteArrayOutputStream baos =
                                        new ByteArrayOutputStream();
                                    ImageIO.write(bimg, "png", baos);
                                    baos.flush();
                                    byte[] imgBytes = baos.toByteArray();
                                    baos.close();

                                    // Insert image into docx
                                    XWPFParagraph imgParagraph =
                                        docx.createParagraph();
                                    XWPFRun imgRun = imgParagraph.createRun();
                                    try (
                                        InputStream picIs =
                                            new ByteArrayInputStream(imgBytes)
                                    ) {
                                        // Units.toEMU converts pixels to EMU (approx; POI uses 96 DPI assumption)
                                        int widthEMU = Units.toEMU(targetW);
                                        int heightEMU = Units.toEMU(targetH);
                                        imgRun.addBreak();
                                        imgRun.addPicture(
                                            picIs,
                                            Document.PICTURE_TYPE_PNG,
                                            xObjectName.getName() + ".png",
                                            widthEMU,
                                            heightEMU
                                        );
                                    } catch (Exception imgEx) {
                                        logger.warn(
                                            "Task ID {}: Không thể chèn ảnh {} trang {}: {}",
                                            taskId,
                                            xObjectName.getName(),
                                            i,
                                            imgEx.getMessage()
                                        );
                                    }
                                }
                            } catch (Exception xEx) {
                                // If a resource can't be read, log and continue
                                logger.debug(
                                    "Task ID {}: Bỏ qua XObject {} trên trang {}: {}",
                                    taskId,
                                    xObjectName,
                                    i,
                                    xEx.getMessage()
                                );
                            }
                        }
                    }

                    // Add a page break after each page except the last one
                    if (i < numPages) {
                        XWPFParagraph breakPara = docx.createParagraph();
                        breakPara.setPageBreak(true);
                    }
                } // end pages loop

                // After composing the document, determine output filename/location
                // Decide result location based on the original PDF path and task metadata.
                String originalFileName = null;
                try {
                    Task t = taskDAO.getTaskById(taskId);
                    if (t != null) {
                        originalFileName = t.getFileName();
                    }
                } catch (Exception ignore) {
                    /* fallback below */
                }

                if (
                    originalFileName == null ||
                    originalFileName.trim().isEmpty()
                ) {
                    originalFileName = pdfFile.getName();
                }
                String baseName = originalFileName;
                int dot = baseName.lastIndexOf('.');
                if (dot > 0) baseName = baseName.substring(0, dot);
                String timestamp = String.valueOf(System.currentTimeMillis());
                String resultFileName = timestamp + "-" + baseName + ".docx";

                String pdfPathStr = pdfFilePath;
                String queueSegment = File.separator + "Queue" + File.separator;
                Path resultsDirPath;
                if (pdfPathStr.contains(queueSegment)) {
                    String replaced = pdfPathStr.replaceFirst(
                        java.util.regex.Pattern.quote(queueSegment),
                        java.util.regex.Matcher.quoteReplacement(
                            File.separator + "Results" + File.separator
                        )
                    );
                    Path maybe = Paths.get(replaced).getParent();
                    if (maybe != null) {
                        resultsDirPath = maybe;
                    } else {
                        resultsDirPath = Paths.get(RESULT_DIR)
                            .resolve("nppp2d_saves")
                            .resolve("Results")
                            .resolve("unknown");
                    }
                } else {
                    resultsDirPath = Paths.get(RESULT_DIR)
                        .resolve("nppp2d_saves")
                        .resolve("Results")
                        .resolve("unknown");
                }

                try {
                    Files.createDirectories(resultsDirPath);
                } catch (IOException e) {
                    logger.warn(
                        "Task ID {}: Không thể tạo thư mục Results {}, sẽ cố ghi vào thư mục mặc định. Lỗi: {}",
                        taskId,
                        resultsDirPath,
                        e.getMessage()
                    );
                    resultsDirPath = Paths.get(RESULT_DIR);
                    try {
                        Files.createDirectories(resultsDirPath);
                    } catch (IOException ex) {
                        /* ignore further */
                    }
                }

                finalResultPath = resultsDirPath.resolve(resultFileName);

                // Write DOCX to disk
                try (
                    FileOutputStream out = new FileOutputStream(
                        finalResultPath.toFile()
                    )
                ) {
                    docx.write(out);
                }
            } catch (IOException pdfEx) {
                // handle PDF reading/writing exceptions
                logger.error(
                    "Task ID {}: Lỗi khi đọc/trích xuất PDF (path={}): {}",
                    taskId,
                    pdfFilePath,
                    pdfEx.getMessage(),
                    pdfEx
                );
                try {
                    taskDAO.updateTaskStatus(taskId, "FAILED");
                } catch (SQLException ignored) {}
                try {
                    docx.close();
                } catch (IOException ignored) {}
                return;
            } finally {
                try {
                    docx.close();
                } catch (IOException ignored) {}
            }

            // Compute word count and update DB
            String fullText = fullTextBuilder.toString();
            int wordCount = 0;
            if (fullText != null && !fullText.trim().isEmpty()) {
                wordCount = fullText.trim().split("\\s+").length;
            }

            resultPath = finalResultPath.toString();
            String summary =
                "Hoàn thành. Số trang: (unknown)  Số từ: " + wordCount;
            // If we can determine pages, we could add it to summary; left as "unknown" to avoid extra DB calls.
            taskDAO.updateTaskResult(taskId, summary, resultPath);
            taskDAO.updateTaskStatus(taskId, "COMPLETED");

            // Optionally clean up original PDF in the Queue directory
            File originalPdfFile = new File(pdfFilePath);
            if (originalPdfFile.exists()) {
                if (!originalPdfFile.delete()) {
                    logger.warn(
                        "Task ID {}: Không thể xóa file PDF gốc: {}",
                        taskId,
                        originalPdfFile.getAbsolutePath()
                    );
                } else {
                    logger.info("Task ID {}: Đã dọn dẹp file PDF gốc.", taskId);
                }
            }

            logger.info(
                "Task ID {}: Xử lý thành công. Kết quả đã lưu vào {}. Số từ: {}",
                taskId,
                resultPath,
                wordCount
            );
        } catch (SQLException eSQL) {
            logger.error(
                "Lỗi DB Worker Task {}: {}",
                taskId,
                eSQL.getMessage(),
                eSQL
            );
            try {
                taskDAO.updateTaskStatus(taskId, "FAILED");
            } catch (SQLException ignored) {}
        } catch (Exception e) {
            logger.error(
                "Lỗi Xử lý File Worker Task {}: {}",
                taskId,
                e.getMessage(),
                e
            );
            try {
                taskDAO.updateTaskStatus(taskId, "FAILED");
            } catch (SQLException ignored) {}
        }
    }
}
