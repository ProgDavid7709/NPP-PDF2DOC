package com.nppp2d.controller;

import com.nppp2d.model.bean.User;
import com.nppp2d.model.dao.UserDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/login")
public class LoginController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(
        LoginController.class
    );
    private UserDAO userDAO;

    public void init() {
        userDAO = new UserDAO();
    }

    /**
     * Hiển thị trang đăng nhập.
     * Nếu user đã đăng nhập (tồn tại session), chuyển hướng họ về trang chính (ví dụ: /home).
     */
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        HttpSession session = request.getSession(false); // Lấy session, không tạo mới

        // Debug: log session information for diagnosing redirect loops
        logger.debug("LoginController.doGet - session object: {}", session);

        // Log request-level details to identify the initiator of repeated /login calls
        logger.debug(
            "LoginController.doGet - requestURI={}, query={}, referer={}, user-agent={}, accept={}",
            request.getRequestURI(),
            request.getQueryString(),
            request.getHeader("Referer"),
            request.getHeader("User-Agent"),
            request.getHeader("Accept")
        );

        if (session != null) {
            try {
                logger.debug(
                    "LoginController.doGet - sessionId={}, userId={}, username={}",
                    session.getId(),
                    session.getAttribute("userId"),
                    session.getAttribute("username")
                );
            } catch (IllegalStateException ise) {
                // session.getId() can throw if session invalidated concurrently
                logger.debug(
                    "LoginController.doGet - session invalidated or inaccessible"
                );
            }
        }

        // Kiểm tra xem người dùng đã đăng nhập chưa (kiểm tra userId để tránh vòng redirect)
        if (session != null && session.getAttribute("userId") != null) {
            // Nếu đã đăng nhập, nhưng có thể request này là fetch/ajax/asset được trigger từ trang khác
            // (ví dụ client-side fetch từ /status). Trường hợp đó không nên redirect vì gây vòng lặp.
            String referer = request.getHeader("Referer");
            String requestedWith = request.getHeader("X-Requested-With");
            String secFetchDest = request.getHeader("Sec-Fetch-Dest");

            // Thêm logging để nhìn rõ loại request
            logger.debug(
                "LoginController.doGet - referer={}, X-Requested-With={}, Sec-Fetch-Dest={}",
                referer,
                requestedWith,
                secFetchDest
            );

            // Nếu request đến /login nhưng referer là /dashboard (tức là bị asset/script gọi từ trang dashboard),
            // trả về 204 No Content để tránh vòng redirect giữa /login và /dashboard.
            if (
                referer != null &&
                referer.contains(request.getContextPath() + "/dashboard")
            ) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            // Nếu đây là một AJAX/fetch/asset request (non-navigation), trả về 204 để client không bị redirect
            // - X-Requested-With: typical for old-school AJAX
            // - Sec-Fetch-Dest != document: newer browsers set this for non-navigation requests
            if (
                (requestedWith != null &&
                    "XMLHttpRequest".equalsIgnoreCase(requestedWith)) ||
                (secFetchDest != null &&
                    !secFetchDest.equalsIgnoreCase("document"))
            ) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            // Mặc định: điều hướng người dùng đã đăng nhập đến trang chính (process)
            response.sendRedirect(request.getContextPath() + "/process"); // Chuyển về servlet/process chính
        } else {
            // Nếu chưa đăng nhập, hiển thị form login
            request
                .getRequestDispatcher("/WEB-INF/jsp/login.jsp")
                .forward(request, response);
        }
    }

    /**
     * Xử lý dữ liệu form đăng nhập.
     */
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Gọi API DAO để xác thực
        User user = userDAO.validateLogin(username, password);

        if (user != null) {
            // --- ĐĂNG NHẬP THÀNH CÔNG ---

            // 1. Tạo một Session mới (nếu chưa có)
            HttpSession session = request.getSession();

            // 2. Lưu thông tin người dùng vào Session (THEO YÊU CẦU)
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());

            // 3. Đặt thời gian timeout cho session (ví dụ: 30 phút)
            session.setMaxInactiveInterval(30 * 60);

            // Debug: log session info immediately after login
            logger.debug(
                "LoginController.doPost - login successful for username={}",
                user.getUsername()
            );
            try {
                logger.debug(
                    "LoginController.doPost - sessionId={}, userId={}, username={}",
                    session.getId(),
                    session.getAttribute("userId"),
                    session.getAttribute("username")
                );
            } catch (IllegalStateException ise) {
                logger.debug(
                    "LoginController.doPost - session invalidated or inaccessible after creation"
                );
            }

            // 4. Chuyển hướng đến trang Process sau khi đăng nhập
            response.sendRedirect(request.getContextPath() + "/process"); // Redirect to the process page
        } else {
            // --- ĐĂNG NHẬP THẤT BẠI ---

            // 1. Gửi thông báo lỗi về lại trang login
            request.setAttribute(
                "errorMessage",
                "Sai tên đăng nhập hoặc mật khẩu."
            );

            // 2. Forward (không phải redirect) để giữ lại request và hiển thị lỗi
            request
                .getRequestDispatcher("/WEB-INF/jsp/login.jsp")
                .forward(request, response);
        }
    }
}
