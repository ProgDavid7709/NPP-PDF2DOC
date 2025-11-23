package com.nppp2d.controller;

import com.nppp2d.model.bean.Task;
import com.nppp2d.model.dao.TaskDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/dashboard")
public class StatusServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(
        StatusServlet.class
    );
    private TaskDAO taskDAO;

    public void init() {
        taskDAO = new TaskDAO();
    }

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        // Structured debug logging via SLF4J to replace ad-hoc System.out calls.
        // logger.debug(
        //     "StatusServlet.doGet - requestURI={}, query={}",
        //     request.getRequestURI(),
        //     request.getQueryString()
        // );
        // logger.debug("StatusServlet.doGet - session object: {}", session);

        // Cookie header & cookies array
        // String cookieHeader = request.getHeader("Cookie");
        // logger.debug("StatusServlet.doGet - Cookie header: {}", cookieHeader);

        if (request.getCookies() != null) {
            logger.debug(
                "StatusServlet.doGet - Cookies array: {}",
                java.util.Arrays.toString(request.getCookies())
            );
        } else {
            logger.debug("StatusServlet.doGet - Cookies array: null");
        }

        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            // logger.debug("StatusServlet.doGet - Request headers:");
            // while (headerNames.hasMoreElements()) {
            //     String name = headerNames.nextElement();
            //     logger.debug("  {}: {}", name, request.getHeader(name));
            // }
        }

        if (session != null) {
            try {
                System.out.println(
                    "[DEBUG] StatusServlet.doGet - sessionId=" +
                        session.getId() +
                        ", userId=" +
                        session.getAttribute("userId") +
                        ", username=" +
                        session.getAttribute("username")
                );
            } catch (IllegalStateException ise) {
                System.out.println(
                    "[DEBUG] StatusServlet.doGet - session invalidated or inaccessible"
                );
            }
        }

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        int currentUserId = (int) session.getAttribute("userId");

        try {
            List<Task> tasks = taskDAO.getTasksByUserId(currentUserId);
            request.setAttribute("taskList", tasks);
            request
                .getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp")
                .forward(request, response);
        } catch (SQLException e) {
            // Log DB error and forward to the internal index page under WEB-INF to avoid external redirects.
            // Forwarding to a JSP under WEB-INF prevents a client-side redirect to /login which caused
            // a redirect loop in some flows (login -> status -> index.jsp -> login ...).
            logger.error(
                "StatusServlet.doGet - DB error when fetching tasks: {}",
                e.getMessage(),
                e
            );
            request.setAttribute(
                "error",
                "Lỗi DB khi lấy danh sách Task: " + e.getMessage()
            );
            // Forward to the protected index JSP (no redirect) to show an error/landing page
            request
                .getRequestDispatcher("/WEB-INF/jsp/index.jsp")
                .forward(request, response);
        }
    }
}
