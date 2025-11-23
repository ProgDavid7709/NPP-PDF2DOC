package com.nppp2d.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IndexServlet
 *
 * Mapped to /index and used as the welcome resource when the welcome-file
 * in web.xml is set to "index". This servlet simply forwards requests to
 * the landing JSP located at /WEB-INF/jsp/index.jsp.
 *
 * Using a servlet as a welcome resource allows the container to dispatch
 * to a servlet (and then forward internally) without requiring an index.jsp
 * file at the web root.
 */
@WebServlet("/index")
public class IndexServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(
        IndexServlet.class
    );

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        // Optional debug logging to help trace welcome requests
        logger.debug(
            "IndexServlet.doGet - requestURI={}, remoteAddr={}",
            request.getRequestURI(),
            request.getRemoteAddr()
        );

        // If the user is already logged in, redirect them to the main process page.
        // Use getSession(false) so we don't create a new session for anonymous visitors.
        javax.servlet.http.HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            response.sendRedirect(request.getContextPath() + "/process");
            return;
        }

        // Forward internally to the landing JSP under WEB-INF for anonymous users
        request
            .getRequestDispatcher("/WEB-INF/jsp/index.jsp")
            .forward(request, response);
    }

    @Override
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        // Handle POST same as GET
        doGet(request, response);
    }
}
