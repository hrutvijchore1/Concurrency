
import java.util.concurrent.locks.*;

class ReadWriteLock {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition canRead = lock.newCondition();
    private final Condition canWrite = lock.newCondition();

    private int readers = 0;
    private boolean isWriteLocked = false;

    public void acquireReadLock() throws InterruptedException {
        lock.lock();
        try {
            while (isWriteLocked) {
                canRead.await();
            }
            readers++;
        } finally {
            lock.unlock();
        }
    }

    public void releaseReadLock() {
        lock.lock();
        try {
            readers--;
            if (readers == 0) {
                canWrite.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public void acquireWriteLock() throws InterruptedException {
        lock.lock();
        try {
            while (isWriteLocked || readers > 0) {
                canWrite.await();
            }
            isWriteLocked = true;
        } finally {
            lock.unlock();
        }
    }

    public void releaseWriteLock() {
        lock.lock();
        try {
            isWriteLocked = false;
            canRead.signalAll();
            canWrite.signal();
        } finally {
            lock.unlock();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}