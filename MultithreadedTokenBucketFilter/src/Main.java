
import java.util.*;

class MultithreadedTokenBucketFilter{
    private long possibleToken=0;
    private final int maxTokens;
    private final int oneSec=1000;

    private MultithreadedTokenBucketFilter(int maxToken){
        this.maxTokens = maxToken;
    }

    static public MultithreadedTokenBucketFilter makeTokenBucketFilter(int capacity) {
        MultithreadedTokenBucketFilter tbf = new MultithreadedTokenBucketFilter(capacity);
        tbf.initialize();
        return tbf;
    }

    void initialize(){
        Thread thread = new Thread(()-> daemonThread());
        thread.setDaemon(true);
        thread.start();
    }

    private void daemonThread() {
        while(true){
            synchronized (this){
                if(possibleToken<maxTokens)
                    possibleToken++;
                this.notifyAll();
            }
            try{
                Thread.sleep(oneSec);
            }catch (InterruptedException e){
                System.out.println(e);
            }
        }
    }

    void getToken() throws InterruptedException{
        synchronized (this){
            while(possibleToken==0)
                this.wait();
            possibleToken--;
            System.out.println(
                    "Granting " + Thread.currentThread().getName() + " token at " + System.currentTimeMillis() / 1000);
        }
    }

}

public class Main {
    public static void main(String[] args) throws InterruptedException {

        Set<Thread> allThreads = new HashSet<Thread>();
        MultithreadedTokenBucketFilter tokenBucketFilter = MultithreadedTokenBucketFilter.makeTokenBucketFilter(1);

        for (int i = 0; i < 10; i++) {

            Thread thread = new Thread(new Runnable() {

                public void run() {
                    try {
                        tokenBucketFilter.getToken();
                    } catch (InterruptedException ie) {
                        System.out.println("We have a problem");
                    }
                }
            });
            thread.setName("Thread_" + (i + 1));
            allThreads.add(thread);
        }

        for (Thread t : allThreads) {
            t.start();
        }

        for (Thread t : allThreads) {
            t.join();
        }

    }
}