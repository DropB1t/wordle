package wordle.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * WorkersThreadPool is a custom thread pool of workers that will allow to ServerController to select new Ready Keys
 * without need to process incoming Requests ( performance optimization for IO-bound and CPU-bound tasks )
*/
public class WorkersThreadPool extends ThreadPoolExecutor {
    
    public WorkersThreadPool(int maximumPoolSize) {
        super(maximumPoolSize/2, maximumPoolSize, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
        this.allowCoreThreadTimeOut(true);
    }
    
}
