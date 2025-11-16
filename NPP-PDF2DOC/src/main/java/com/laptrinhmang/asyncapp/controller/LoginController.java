package com.laptrinhmang.asyncapp.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.laptrinhmang.asyncapp.model.bean.User;
import com.laptrinhmang.asyncapp.model.dao.UserDAO;

@WebServlet("/login")
public class LoginController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    public void init() {
        userDAO = new UserDAO();
    }

    /**
     * Hiển thị trang đăng nhập.
     * Nếu user đã đăng nhập (tồn tại session), chuyển hướng họ về trang chính (ví dụ: /home).
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false); // Lấy session, không tạo mới
        
        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (session != null && session.getAttribute("username") != null) {
            // Nếu đã đăng nhập, chuyển hướng về trang chủ
            response.sendRedirect(request.getContextPath() + "/status"); // Giả sử /home là trang chính
        } else {
            // Nếu chưa đăng nhập, hiển thị form login
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý dữ liệu form đăng nhập.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
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
            
            // 4. Chuyển hướng đến trang chính sau khi đăng nhập
            response.sendRedirect(request.getContextPath() + "/status"); // Giả sử /home là trang chính

        } else {
            // --- ĐĂNG NHẬP THẤT BẠI ---
            
            // 1. Gửi thông báo lỗi về lại trang login
            request.setAttribute("errorMessage", "Sai tên đăng nhập hoặc mật khẩu.");
            
            // 2. Forward (không phải redirect) để giữ lại request và hiển thị lỗi
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        }
    }
}