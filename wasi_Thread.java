import java.util.concurrent.atomic.AtomicLong;

public class Wasi_Thread implements Runnable {

    static AtomicLong safeStatic = new AtomicLong(0) 
    static long unsafeStatic = 0;
    static boolean threadSafe;
    static long increments;

    long instanceCount = 0;                     

    public void run() {
        for (long i = 0; i < increments; i++) {
            instanceCount++;
            if (threadSafe)
                safeStatic.incrementAndGet();
            else
                unsafeStatic++;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int n = Integer.parseInt(args[0]);
        increments = Long.parseLong(args[1]);
        threadSafe = Boolean.parseBoolean(args[2]);

        Wasi_Thread[] tasks = new Wasi_Thread[n];
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            tasks[i] = new Wasi_Thread();
            threads[i] = new Thread(tasks[i]);
            threads[i].start();
        }
        for (int i = 0; i < n; i++)
            threads[i].join();

        long expected = (long) n * increments;
        long staticCount = threadSafe ? safeStatic.get() : unsafeStatic;
        long nonStatic = 0;
        for (Wasi_Thread t : tasks)
            nonStatic += t.instanceCount;

        long diff = Math.abs(staticCount - nonStatic);
        double pct = (nonStatic == 0) ? 0.0 : diff * 100.0 / nonStatic;

        System.out.println("Mode            : " + (threadSafe ? "Thread-safe (AtomicLong)" : "Unsynchronized (long)"));
        System.out.println("Threads (n)     : " + n);
        System.out.println("Increments (m)  : " + increments);
        System.out.println("Expected count  : " + expected);
        System.out.println("Static count    : " + staticCount);
        System.out.println("Non-static total: " + nonStatic);
        System.out.println("Absolute diff   : " + diff);
        System.out.printf("Difference (%%)  : %.4f%%%n", pct);
    }
}