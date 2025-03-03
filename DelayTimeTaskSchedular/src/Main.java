import java.util.Collection;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ScheduledThreadPoolExecutor {
    private final int threadCount;
    private final WorkerThread[] workers;
    private final PriorityQueue<ScheduledTask> taskQueue;
    private volatile boolean isShutdown = false;
    private final Lock lock = new ReentrantLock();
    private final Condition taskAvailable = lock.newCondition();



    public ScheduledThreadPoolExecutor(int threadCount){
        taskQueue = new PriorityQueue<>();
        workers = new WorkerThread[threadCount];
        this.threadCount = threadCount;
        for(int i=0;i<threadCount;i++) {
            workers[i] = new WorkerThread();
            workers[i].start();
        }
    }

    public void schedule(Runnable task, long delay, TimeUnit unit){
        long executionTime = System.currentTimeMillis() + unit.toMillis(delay);
        lock.lock();
        try{
            if (isShutdown) {
                throw new IllegalStateException("ExecutorService is shutdown");
            }
            taskQueue.add(new ScheduledTask(task, executionTime));
            taskAvailable.signalAll(); // Notify waiting worker thread
        }finally{
            lock.unlock();
        }
    }

    public void shutdown() {
        lock.lock();
        try {
            isShutdown = true;
            taskAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }



    private class WorkerThread extends Thread{
        public void run(){
            while(true){
                Runnable task=null;
                lock.lock();
                try{
                    while(taskQueue.isEmpty() || taskQueue.peek().executionTime > System.currentTimeMillis()) {
                        if (isShutdown)
                            return;
                        try {
                            long sleepTime = taskQueue.isEmpty() ? (Long.MAX_VALUE) : taskQueue.peek().executionTime - System.currentTimeMillis();
                            if (sleepTime > 0) {
                                taskAvailable.await(sleepTime,TimeUnit.MILLISECONDS);
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    task = taskQueue.poll().task;
                }finally {
                    lock.unlock();
                }
                task.run();
            }
        }
    }

    private static class ScheduledTask implements Comparable<ScheduledTask>{
        private final Runnable task;
        private final Long executionTime;

        public ScheduledTask(Runnable task, Long executionTime){
            this.task = task;
            this.executionTime = executionTime;
        }

        @Override
        public int compareTo(ScheduledTask e){
            return Long.compare(this.executionTime,e.executionTime);
        }
    }

}



public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}