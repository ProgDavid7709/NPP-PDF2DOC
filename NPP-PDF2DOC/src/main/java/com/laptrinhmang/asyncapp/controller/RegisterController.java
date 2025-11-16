package com.laptrinhmang.asyncapp.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.laptrinhmang.asyncapp.model.bean.User;
import com.laptrinhmang.asyncapp.model.dao.UserDAO;

@WebServlet("/register")
public class RegisterController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    public void init() {
        userDAO = new UserDAO();
    }

    /**
     * Hiển thị trang đăng ký.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
    }

    /**
     * Xử lý dữ liệu form đăng ký.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // --- VALIDATE ĐƠN GIẢN ---
        
        // 1. Kiểm tra mật khẩu có khớp không
        if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu không khớp. Vui lòng thử lại.");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        // 2. Tạo đối tượng User
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password); 

        // 3. Gọi API DAO để tạo user
        boolean isSuccess = userDAO.createUser(newUser);

        if (isSuccess) {
            // --- ĐĂNG KÝ THÀNH CÔNG ---
            // Gửi thông báo thành công và chuyển về trang đăng nhập
            request.setAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        } else {
            // --- ĐĂNG KÝ THẤT BẠI (Thường là do trùng username) ---
            request.setAttribute("errorMessage", "Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
        }
    }
}