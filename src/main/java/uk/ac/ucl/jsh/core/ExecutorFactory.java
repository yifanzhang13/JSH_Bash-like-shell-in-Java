package uk.ac.ucl.jsh.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorFactory {
    private static volatile ExecutorService executorService;

    public static ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newCachedThreadPool();
        }

        return executorService;
    }

    public static synchronized void reset() {
        executorService = Executors.newCachedThreadPool();
    }
}
