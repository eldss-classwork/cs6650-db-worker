import data.DataSource;

/**
 * Simply starts consumer threads and waits for a shutdown signal.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Starting consumer threads");

        int poolSize = DataSource.getPoolSize();
        for (int i = 0; i < poolSize; i++) {
            Thread worker = new Thread(new DbWriteWorker());
            worker.start();
        }

        System.out.println("Workers started. Press Ctrl+C to stop.");
    }
}
