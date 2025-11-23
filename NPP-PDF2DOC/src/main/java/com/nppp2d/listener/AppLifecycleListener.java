package com.nppp2d.listener;

import com.nppp2d.model.bo.TaskQueueBO;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class AppLifecycleListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(
        AppLifecycleListener.class
    );

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        TaskQueueBO.shutdown();
        logger.info("--- Task Queue Service Shut Down Safely ---");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("--- Task Queue Service Initialized ---");
        // TaskQueueBO tự khởi tạo do ExecutorService là static
    }
}
