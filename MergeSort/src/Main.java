import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

class MultiThreadingMergeSort {
    private final int[] input;
    private final int[] scratch;
    private final ExecutorService threadPool;

    public MultiThreadingMergeSort(int[] input, ExecutorService threadPool) {
        this.input = input;
        this.scratch = new int[input.length];
        this.threadPool = threadPool;
    }

    void divide(int start, int end) {
        if (start >= end) {
            return;
        }

        int mid = start + (end - start) / 2;

        // Limit the number of threads to avoid excessive parallelism
        if (end - start > 5) {  // Threshold to decide when to use threads
            Future<?> leftSort = threadPool.submit(() -> divide(start, mid));
            Future<?> rightSort = threadPool.submit(() -> divide(mid + 1, end));

            try {
                leftSort.get();  // Wait for left half to complete
                rightSort.get(); // Wait for right half to complete
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            divide(start, mid);
            divide(mid + 1, end);
        }

        merge(start, mid, end);
    }

    void merge(int start, int mid, int end) {
        System.arraycopy(input, start, scratch, start, end - start + 1);

        int i = start;
        int j = mid + 1;
        int k = start;

        while (i <= mid && j <= end) {
            if (scratch[i] <= scratch[j]) {
                input[k++] = scratch[i++];
            } else {
                input[k++] = scratch[j++];
            }
        }

        while (i <= mid) {
            input[k++] = scratch[i++];
        }

        while (j <= end) {
            input[k++] = scratch[j++];
        }
    }
}

public class Main {
    private static final int SIZE = 25;
    private static final Random random = new Random(System.currentTimeMillis());

    static void createTestData(int[] input) {
        for (int i = 0; i < SIZE; i++) {
            input[i] = random.nextInt(10000);
        }
    }

    static void printArray(int[] input) {
        System.out.println(Arrays.toString(input));
    }

    public static void main(String[] args) {
        int[] input = new int[SIZE];
        createTestData(input);

        System.out.println("Unsorted Array:");
        printArray(input);

        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        MultiThreadingMergeSort sorter = new MultiThreadingMergeSort(input, threadPool);

        long startTime = System.currentTimeMillis();
        sorter.divide(0, input.length - 1);
        long endTime = System.currentTimeMillis();

        threadPool.shutdown();

        System.out.println("\nTime taken to sort = " + (endTime - startTime) + " milliseconds");
        System.out.println("Sorted Array:");
        printArray(input);
    }
}
