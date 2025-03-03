
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class TaskScheduler {
    private static class  Node{
        Runnable task;
        Node next;
        Node(Runnable task){
            this.task = task;
        }
    }
    private Node head = null , tail =null;
    private boolean isRunning = true;
    private int taskCount=0;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition taskAvailable = lock.newCondition();
    private final Condition allTasksCompleted = lock.newCondition();

    public TaskScheduler(){
        Thread worker = new Thread(this:: workerLoop);
        worker.setDaemon(true);
        worker.start();
    }
    public void schedule(Runnable task){
        lock.lock();
        try{
            Node newNode = new Node(task);
            if(tail==null){
                head=tail=newNode;
            }else{
                tail.next = newNode;
                tail=tail.next;
            }
            taskCount++;
            taskAvailable.signal();
        }finally{
            lock.unlock();
        }
    }

    public void waitUntilComplete() throws InterruptedException {
        lock.lock();
        try{
            while(taskCount>0)
                allTasksCompleted.await();
        }finally{
            lock.unlock();
        }
    }

    private void workerLoop() {
        while (isRunning) {
            Runnable task = null;
            lock.lock();
            try {
                while (head == null) {
                    taskAvailable.await();
                }
                task = head.task;
                head = head.next;
                if (head == null)
                    tail = null;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
            if (task != null) {
                try {
                    task.run();
                } finally {
                    lock.lock();
                    try {
                        taskCount--;
                        if (taskCount == 0)
                            allTasksCompleted.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {

        TaskScheduler scheduler = new TaskScheduler();

        scheduler.schedule(() -> {
            System.out.println("Task 1 executed by " + Thread.currentThread().getName());
        });

        scheduler.schedule(() -> {
            System.out.println("Task 2 executed by " + Thread.currentThread().getName());
        });

        scheduler.waitUntilComplete();
        System.out.println("All tasks completed.");
    }
}