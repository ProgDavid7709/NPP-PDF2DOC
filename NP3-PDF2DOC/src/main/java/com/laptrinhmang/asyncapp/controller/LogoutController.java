package com.laptrinhmang.asyncapp.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Lấy session hiện tại (không tạo mới nếu không có)
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // 2. Vô hiệu hóa session (xóa tất cả attribute, bao gồm userId và username)
            session.invalidate();
        }
        
        // 3. Chuyển hướng người dùng về trang đăng nhập
        // Luôn sử dụng getContextPath() để đảm bảo đường dẫn đúng
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
