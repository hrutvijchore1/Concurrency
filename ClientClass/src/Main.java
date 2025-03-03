import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

abstract class Client {
    public void init() {}

    public void request() throws IllegalAccessException {}

    public void close() {}
}

class ClientWrapper extends Client {

    private Connection connection = null;
    private final Lock lock = new ReentrantLock();

    private final Condition initCondition = lock.newCondition();
    private final Condition closeCondition = lock.newCondition();

    private boolean isInitializing = false; // Tracks if init() is in progress
    private boolean isClosed = false; // Prevents multiple close() calls
    private int requestCount = 0; // Tracks active requests

    @Override
    public void init() {
        lock.lock();
        try {
            if (connection != null) return; // Already initialized, return immediately

            isInitializing = true; // Mark that init is in progress

            connection = new Connection();

            isInitializing = false; // Mark that init is complete
            initCondition.signalAll(); // Wake up waiting threads

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void request() throws IllegalAccessException {
        lock.lock();
        try {
            while (isInitializing) {
                initCondition.await(); // Wait if init() is still in progress
            }

            if (connection == null || isClosed) {
                throw new IllegalAccessException("Connection is closed");
            }

            requestCount++; // Track active requests
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }

        // Simulate request handling
        try {
            Thread.sleep(100); // Simulating processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.lock();
            try {
                requestCount--;
                if (requestCount == 0) {
                    closeCondition.signalAll(); // Wake up close() if it was waiting
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            while (requestCount > 0) {
                closeCondition.await(); // Wait if requests are still in progress
            }

            if (isClosed) return; // Prevent multiple closes

            connection = null;
            isClosed = true; // Mark connection as closed

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }
}
