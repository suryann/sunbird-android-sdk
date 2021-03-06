package org.ekstep.genieservices.eventbus;

import org.greenrobot.eventbus.util.AsyncExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 26/4/17.
 *
 * @author swayangjit
 */
public class EventPublisherThreadPool {

    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    private static volatile AsyncExecutor asyncExecutor;

    private EventPublisherThreadPool() {
    }

    public static AsyncExecutor getInstance() {
        if (asyncExecutor == null) {
            synchronized (EventPublisherThreadPool.class) {
                if (asyncExecutor == null) {
                    AsyncExecutor.Builder builder = AsyncExecutor.builder();
                    ExecutorService threadPool =
                            Executors.newFixedThreadPool(MAX_THREADS);
                    builder.threadPool(threadPool);
                    asyncExecutor = builder.build();
                }
            }
        }
        return asyncExecutor;
    }
}
