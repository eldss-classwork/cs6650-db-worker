/**
 * Simply starts consumer threads and waits for a shutdown signal.
 */
public class Main {

    // TODO: Make the same number as db connection pool size
    private static final int NUM_THREADS = 20;

    public static void main(String[] args) {
        System.out.println("Starting consumer threads");

        for (int i = 0; i < NUM_THREADS; i++) {
            Thread worker = new Thread(new DbWriteWorker());
            worker.start();
        }

        System.out.println("Workers started. Press Ctrl+C to stop.");
    }
}
