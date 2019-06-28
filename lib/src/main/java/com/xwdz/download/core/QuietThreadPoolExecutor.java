package com.xwdz.download.core;

import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author xingwei.huang (xwdz9989@gmail.com)
 * @since v1.0.0
 */
public class QuietThreadPoolExecutor {

    private static ExecutorService sThreadPoolExecutor;

    static {
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "QuietThreadPoolExecutor");
            }
        };
        sThreadPoolExecutor = Executors.newCachedThreadPool(threadFactory);
    }

    public static void setExecutorService(ExecutorService threadPoolExecutor) {
        sThreadPoolExecutor = threadPoolExecutor;
    }

    public static ExecutorService getThreadPool() {
        return sThreadPoolExecutor;
    }

    public static void execute(@NonNull Runnable command) {
        sThreadPoolExecutor.execute(command);
    }

    public static void submit(@NonNull Callable callable) {
        sThreadPoolExecutor.submit(callable);

    }

}
