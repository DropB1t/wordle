package wordle.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkersThreadPool extends ThreadPoolExecutor {

    public WorkersThreadPool(int maximumPoolSize) {
        super(maximumPoolSize/2, maximumPoolSize, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
        this.allowCoreThreadTimeOut(true);
    }
    
}
