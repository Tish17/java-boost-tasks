package loom;

public class ThreadDemo {

    private static final int THREAD_COUNT = 1000;

    public static void main(String[] args) {
        virtualThreads();
        platformThreads();
    }

    private static void virtualThreads() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread.startVirtualThread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        long end = System.currentTimeMillis();
        System.out.println("Created " + THREAD_COUNT + " virtual threads in " + (end - start) + "ms");
    }

    private static void platformThreads() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
        long end = System.currentTimeMillis();
        System.out.println("Created " + THREAD_COUNT + " platform threads in " + (end - start) + "ms");
    }
}
