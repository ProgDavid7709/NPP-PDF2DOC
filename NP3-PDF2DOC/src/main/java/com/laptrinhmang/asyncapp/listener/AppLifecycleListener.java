package com.laptrinhmang.asyncapp.listener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import com.laptrinhmang.asyncapp.model.service.TaskQueueService;
@WebListener
public class AppLifecycleListener implements ServletContextListener{
	@Override
    public void contextDestroyed(ServletContextEvent sce) {
        TaskQueueService.shutdown();
        System.out.println("--- Task Queue Service Shut Down Safely ---");
    }
	@Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("--- Task Queue Service Initialized ---");
        // TaskQueueService tự khởi tạo do ExecutorService là static
    }
}