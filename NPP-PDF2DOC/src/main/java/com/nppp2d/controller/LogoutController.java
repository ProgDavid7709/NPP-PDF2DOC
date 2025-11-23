package com.nppp2d.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/logout")
public class LogoutController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(
        LogoutController.class
    );

    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        // 1. Lấy session hiện tại (không tạo mới nếu không có)
        HttpSession session = request.getSession(false);

        if (session != null) {
            // 2. Vô hiệu hóa session (xóa tất cả attribute, bao gồm userId và username)
            session.invalidate();
            logger.info(
                "Session invalidated for logout request from remoteAddr={}",
                request.getRemoteAddr()
            );
        } else {
            logger.debug(
                "Logout request did not have an active session (remoteAddr={})",
                request.getRemoteAddr()
            );
        }

        // 3. Chuyển hướng người dùng về trang chính (index) sau khi đăng xuất
        // Luôn sử dụng getContextPath() để đảm bảo đường dẫn đúng
        response.sendRedirect(request.getContextPath() + "/index");
    }
}
