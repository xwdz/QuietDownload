package com.xwdz.download.core;

import android.support.annotation.NonNull;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author xingwei.huang (xwdz9989@gmail.com)
 * @since v1.0.0
 */
public class QuietExecutors {

    private static final int COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_COUNT = 2 * COUNT + 1;


    private static ThreadPoolExecutor sThreadPoolExecutor;

    static {
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "QuietExecutors");
            }
        };

        sThreadPoolExecutor = new ThreadPoolExecutor(CORE_COUNT, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(), threadFactory);
    }







    public static void execute(@NonNull Runnable command) {
        sThreadPoolExecutor.execute(command);
    }
}
