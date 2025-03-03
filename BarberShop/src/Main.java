import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

class BarberShop{
    final int chairs = 3;
    int waitingCustomer=0;
    Semaphore waitingForCustomerToEnter = new Semaphore(0);
    Semaphore waitingForBarberToGetReady = new Semaphore(0);
    Semaphore waitForBarbeToCutHair = new Semaphore(0);
    Semaphore waitForCustomerToLeave = new Semaphore(0);
    private int haiCutsGiven=0;
    ReentrantLock lock = new ReentrantLock();

    void customerWalksIn() throws InterruptedException {
        lock.lock();
        if(waitingCustomer==chairs){
            System.out.println("Customer walks out, all chairs occupied.");
            lock.unlock();
            return;
        }
        System.out.println("customer acquired the chair");
        waitingCustomer++;
        lock.unlock();

        waitingForCustomerToEnter.release();
        waitingForBarberToGetReady.acquire();

        waitForBarbeToCutHair.acquire();
        waitForCustomerToLeave.release();

    }

    void barber() throws InterruptedException {
        while(true){
            waitingForCustomerToEnter.acquire();
            waitingForBarberToGetReady.release();
            waitingCustomer--;
            haiCutsGiven++;
            System.out.println("Barber cutting hair..." + haiCutsGiven);
            Thread.sleep(50);
            waitForBarbeToCutHair.release();
            waitForCustomerToLeave.acquire();

        }

    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {


        HashSet<Thread> set = new HashSet<Thread>();
        final BarberShop barberShopProblem = new BarberShop();

        Thread barberThread = new Thread(new Runnable() {
            public void run() {
                try {
                    barberShopProblem.barber();
                } catch (InterruptedException ie) {

                }
            }
        });
        barberThread.start();

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        barberShopProblem.customerWalksIn();
                    } catch (InterruptedException ie) {

                    }
                }
            });
            set.add(t);
        }

        for (Thread t : set) {
            Thread.sleep(10);
            t.start();
        }

        for (Thread t : set) {
            t.join();
        }
    }
}