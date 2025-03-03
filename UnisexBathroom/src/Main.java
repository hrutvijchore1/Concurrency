import java.util.concurrent.Semaphore;

class UnisexBathroom{

    static String MEN = "Men";
    static String WOMEN = "Women";
    static String NONE = "None";

    String inUseBy= NONE;
    int empsInBathroom=0;
    Semaphore maxEmps = new Semaphore(3);
    private final Semaphore queueLock = new Semaphore(1, true);   // Ensures fairness (FIFO ordering)


    void useBathroom(String name) throws InterruptedException{
        System.out.println(name + " using bathroom. Current employees in bathroom = " + empsInBathroom);
        Thread.sleep(10000);
        System.out.println(name + " done using bathroom");
    }

    void useMaleBathroom(String name) throws InterruptedException{
        enterBathroom(name, MEN);
    }

    void useFemaleBathroom(String name) throws InterruptedException{
       enterBathroom(name, WOMEN);
    }

    void enterBathroom(String name ,String gender) throws InterruptedException {
        queueLock.acquire();
        synchronized (this){
            while(!inUseBy.equals(NONE) && !inUseBy.equals(gender))
                this.wait();
            if(empsInBathroom==0)
                inUseBy = gender;
            maxEmps.acquire();
            empsInBathroom++;

        }
        queueLock.release();
        useBathroom(name);
        maxEmps.release();
        synchronized (this){
            empsInBathroom--;
            if(empsInBathroom==0)
                inUseBy=NONE;
            notifyAll();
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        final UnisexBathroom unisexBathroom = new UnisexBathroom();

        Thread male1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    unisexBathroom.useMaleBathroom("Male 1");
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        });

        Thread female1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    unisexBathroom.useFemaleBathroom("Female 1");
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        });
        Thread male2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    unisexBathroom.useMaleBathroom("Male 2");
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        });

        Thread male3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    unisexBathroom.useMaleBathroom("Male 3");
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        });
        Thread male4 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    unisexBathroom.useMaleBathroom("Male 4");
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        });

        Thread male5 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    unisexBathroom.useMaleBathroom("Male 5");
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        });


        male2.start();
        male1.start();
        male3.start();
        male4.start();
        female1.start();
        male5.start();

        female1.join();
        male1.join();
        male2.join();
        male3.join();
        male4.join();
        male5.join();
    }
}