package wordle.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import wordle.utils.Util;

/**
 * Custom Shutdown Hook that stop Server Controller and perform shutdown of Thread Pool safely
 */
public class ServerShutdownHook extends Thread {

    private final ServerController server;
    private final ExecutorService workersPool;
    
    public ServerShutdownHook(ServerController server, ExecutorService workersPool) {
        this.server = server;
        this.workersPool = workersPool;
    }

    @Override
    public void run() {
        try {
            this.server.stop();
            this.server.close();
            shutdownWorkers(this.workersPool);
        } catch (Exception e) {
            Util.printException(e);
        }
        Util.memoryStats();
        System.out.print(Util.ConsoleColors.YELLOW + "\n| Shutting down the server |\n" + Util.ConsoleColors.RESET);
    }

    private void shutdownWorkers(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(32, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(32, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            Util.printException(ie);
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
