package csc733.group5;

import java.util.Random;
import java.util.concurrent.*;

public class BenchmarkExecutor {

    private final BenchmarkMetrics metrics = new BenchmarkMetrics();

    // 5 concurrent transactions per benchmark spec; 5 additional queued transactions
    private final ExecutorService exec = Executors.newFixedThreadPool(5);
    // Generate tasks on a single thread
    private final ExecutorService txGen = Executors.newSingleThreadExecutor();

    private final LinkedBlockingQueue<Runnable> pendingTransactions = new LinkedBlockingQueue<>(5);
    private final CountDownLatch startTxGenSemaphore = new CountDownLatch(1);
    private final CountDownLatch startTxProcSemaphore = new CountDownLatch(1);
    private final CountDownLatch endSemaphore = new CountDownLatch(1);

    private final Random rand;

    public BenchmarkExecutor(final Random _rand) {
        rand = _rand;
        for (int i = 0; i < 5; i++) exec.submit(() -> {
            try {
                startTxProcSemaphore.await();
            } catch (final InterruptedException x) {
                throw new RuntimeException("Interrupted waiting on teh BenchmarkExecutor to be started.");
            }
            while (endSemaphore.getCount() > 0) {
                final Runnable nextTx = pendingTransactions.poll();
                if (nextTx == null) {
                    System.out.println("Completing transactions faster than they're being generated!");
                    continue;
                }
                nextTx.run();
            }
        });
        txGen.submit(() -> {
            try {
                startTxGenSemaphore.await();
            } catch (final InterruptedException x) {
                throw new RuntimeException("Interrupted waiting on the BenchmarkExecutor to be started.");
            }
            for (int i = 0; i < 5; i++) pendingTransactions.add(generate());
            startTxProcSemaphore.countDown();
            while (endSemaphore.getCount() > 0) {
                try {
                    pendingTransactions.put(generate());
                } catch (final InterruptedException e) {
                    System.out.println("Interrupted trying to insert a new transaction; maybe shutdown?");
                }
            }
        });
    }

    /**
     * Generate a random transaction with probabilities specified by TPC-C spec
     * @return
     */
    private Runnable generate() {
        return () -> {
            try {
                Thread.sleep(rand.nextInt(4000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            metrics.completedTransaction();
        };
    }

    public final void begin() {
        metrics.start();
        startTxGenSemaphore.countDown();
    }

    public final void end() {
        endSemaphore.countDown();
        metrics.end();
        txGen.shutdownNow();
        exec.shutdown();
    }

    public static void main(final String[] args) throws InterruptedException {
        final BenchmarkExecutor bex = new BenchmarkExecutor(new Random(42));
        bex.begin();
        Thread.sleep(15000);
        bex.end();
        final int completed = bex.metrics.getCompletedTransactions();
        final long elapsedNano = bex.metrics.getRuntimeInMillis();
        System.out.format("Completed %d transactions in %d milliseconds, or %f per second\n",
                completed, elapsedNano, ((double)(completed*1000) / elapsedNano));
    }
}
