package com.nppp2d.model.bo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business Object that manages a shared task queue for background workers.
 * Improvements:
 * - Renamed to BO package/class
 * - Uses named threads via a custom ThreadFactory
 * - Pool size is configurable via system property `worker.pool.size`
 * - Structured logging via SLF4J
 */
public class TaskQueueBO {

    private static final Logger logger = LoggerFactory.getLogger(
        TaskQueueBO.class
    );

    private static final int DEFAULT_POOL_SIZE = Integer.getInteger(
        "worker.pool.size",
        4
    );

    private static final ExecutorService EXECUTOR =
        Executors.newFixedThreadPool(
            DEFAULT_POOL_SIZE,
            new NamedThreadFactory("pdf-worker")
        );

    /**
     * Submit a worker to the shared executor.
     * Adds defensive logging and error handling to surface executor state for debugging.
     */
    public static void submit(Runnable worker) {
        if (worker == null) {
            logger.warn("Attempted to submit null worker to TaskQueueBO");
            return;
        }
        try {
            if (EXECUTOR instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) EXECUTOR;
                int active = tpe.getActiveCount();
                int poolSize = tpe.getPoolSize();
                int queueSize = tpe.getQueue().size();
                logger.debug(
                    "Submitting worker to ThreadPoolExecutor (active={}, poolSize={}, queueSize={})",
                    active,
                    poolSize,
                    queueSize
                );
            } else {
                logger.debug(
                    "Submitting worker to ExecutorService implementation: {}",
                    EXECUTOR.getClass().getName()
                );
            }
            // Wrap the worker so we log when it actually starts/finishes and surface any throwable
            Runnable wrappedWorker = new Runnable() {
                @Override
                public void run() {
                    logger.info(
                        "Worker wrapper starting; executor status before run: {}",
                        getStatus()
                    );
                    try {
                        worker.run();
                        logger.info(
                            "Worker wrapper finished normally; executor status after run: {}",
                            getStatus()
                        );
                    } catch (Throwable t) {
                        logger.error(
                            "Worker wrapper caught throwable while executing worker",
                            t
                        );
                        // rethrow so executor's UncaughtExceptionHandler / logging can also observe it
                        throw t;
                    }
                }
            };
            EXECUTOR.submit(wrappedWorker);
            logger.info("TaskQueueBO status after submit: {}", getStatus());
        } catch (RejectedExecutionException rex) {
            // Common cause: executor is shutting down or saturated.
            logger.error(
                "Task submission rejected: executor may be shutting down or saturated",
                rex
            );
            throw rex;
        } catch (RuntimeException rex) {
            logger.error("Unexpected error when submitting worker", rex);
            throw rex;
        }
    }

    /**
     * Return status information for the executor to help debugging.
     */
    public static String getStatus() {
        if (EXECUTOR instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) EXECUTOR;
            return String.format(
                "ThreadPoolExecutor[poolSize=%d, active=%d, largestPoolSize=%d, completed=%d, taskCount=%d, queueSize=%d, isShutdown=%b, isTerminated=%b]",
                tpe.getPoolSize(),
                tpe.getActiveCount(),
                tpe.getLargestPoolSize(),
                tpe.getCompletedTaskCount(),
                tpe.getTaskCount(),
                tpe.getQueue().size(),
                tpe.isShutdown(),
                tpe.isTerminated()
            );
        } else {
            return (
                "ExecutorService implementation: " +
                EXECUTOR.getClass().getName()
            );
        }
    }

    /**
     * Gracefully shut down the executor, with logging and forced shutdown fallback.
     */
    public static void shutdown() {
        logger.info(
            "Shutting down TaskQueueBO executor. Status before shutdown: {}",
            getStatus()
        );
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn(
                    "Executor did not terminate within timeout; invoking shutdownNow()"
                );
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted while awaiting executor termination", e);
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info(
            "TaskQueueBO executor shutdown complete. Status after shutdown: {}",
            getStatus()
        );
    }

    /**
     * Simple ThreadFactory that names threads and installs an UncaughtExceptionHandler.
     */
    private static class NamedThreadFactory implements ThreadFactory {

        private final AtomicInteger counter = new AtomicInteger(1);
        private final String baseName;

        NamedThreadFactory(String baseName) {
            this.baseName = baseName;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(
                r,
                baseName + "-" + counter.getAndIncrement()
            );
            t.setDaemon(true);
            t.setUncaughtExceptionHandler((thr, ex) ->
                logger.error(
                    "Uncaught exception in worker thread {}",
                    thr.getName(),
                    ex
                )
            );
            return t;
        }
    }
}
