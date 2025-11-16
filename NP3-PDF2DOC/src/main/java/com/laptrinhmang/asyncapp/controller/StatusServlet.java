package com.laptrinhmang.asyncapp.controller;

import com.laptrinhmang.asyncapp.model.dao.TaskDAO;
import com.laptrinhmang.asyncapp.model.bean.ProcessingTask;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/status")
public class StatusServlet extends HttpServlet {
    
    private TaskDAO taskDAO;

    public void init() {
        taskDAO = new TaskDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        int currentUserId = (int) session.getAttribute("userId");

        try {
            List<ProcessingTask> tasks = taskDAO.getTasksByUserId(currentUserId);
            request.setAttribute("taskList", tasks);
            request.getRequestDispatcher("/WEB-INF/jsp/status.jsp").forward(request, response);

        } catch (SQLException e) {
            request.setAttribute("error", "Lỗi DB khi lấy danh sách Task: " + e.getMessage());
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
}