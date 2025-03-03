public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello world!");
        BlockingQueue<Integer> blockingQueue = new BlockingQueue<>(5);

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 50; i++) {
                        blockingQueue.enqueue(i);
                        System.out.println("enqueued " + i);
                    }
                }catch(InterruptedException e){
                    System.out.println(e.getMessage());
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 25; i++) {
                        blockingQueue.dequeue();
                        System.out.println("Thread 2 dequeued: " + i);
                    }
                }catch(InterruptedException e){
                    System.out.println(e.getMessage());
                }
            }
        });

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 25; i++) {
                        blockingQueue.dequeue();
                        System.out.println("Thread 3 dequeued: " + i);
                    }
                }catch(InterruptedException e){
                    System.out.println(e.getMessage());
                }
            }
        });

        thread1.start();
        Thread.sleep(1000);
        thread2.start();
        thread3.start();;
        thread2.join();
//        thread3.start();;
        thread1.join();
        thread3.join();
    }
}