package csc733.group5;

import java.util.concurrent.atomic.AtomicInteger;

public class BenchmarkMetrics {
    private boolean isRunning = false;
    private long startTime = 0L;
    private long endTime = 0L;
    private AtomicInteger completedTransactions = null;

    public void start() {
        completedTransactions = new AtomicInteger(0);
        startTime = System.currentTimeMillis();
        isRunning = true;
    }

    public void end() {
        isRunning = false;
        endTime = System.currentTimeMillis();
    }

    public void completedTransaction() {
        if (isRunning) completedTransactions.getAndIncrement();
    }

    public long getRuntimeInMillis() { return endTime - startTime; }
    public int getCompletedTransactions() { return completedTransactions.get(); }
}
