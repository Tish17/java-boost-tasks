import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HighLoadProcessor {

    // Metrics
    private final AtomicInteger successResponses = new AtomicInteger(0);
    private final AtomicInteger failedResponses = new AtomicInteger(0);

    // Task Management
    private final ExecutorService executor = new ThreadPoolExecutor(5, 10, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Future<?>> taskMap = new ConcurrentHashMap<>();

    // Temp File Storage Directory
    private final File tempDir = new File("temp");

    // Constructor
    public HighLoadProcessor() {
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        cleanupScheduler.scheduleAtFixedRate(this::cleanUpCompletedTasks, 30, 30, TimeUnit.SECONDS);
    }

    // Metrics Management
    public void logSuccessResponse() {
        successResponses.incrementAndGet();
    }

    public void logFailedResponse() {
        failedResponses.incrementAndGet();
    }

    public double getResponseRatio() {
        return successResponses.get() / Math.max((double) failedResponses.get(), 1);
    }

    // Submit Task
    public String submitTask(Callable<Void> task) {
        Future<Void> future = executor.submit(task);
        String taskId = UUID.randomUUID().toString();
        taskMap.put(taskId, future);
        return taskId;
    }

    // Process Request
    public void processRequest(InputStream requestPayload) {
        try (InputStream inputStream = requestPayload) {
            // Step 1: Write payload to temp file
            File tempFile = File.createTempFile("request_" + Thread.currentThread().getName(), ".tmp", tempDir);
            try (OutputStream fileOut = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                }
            }
            // Step 2: Submit Task for Processing
            submitTask(() -> {
                // Simulated task processing
                try {
                    System.out.printf("Processing payload from file: %s\n", tempFile.getName());
                    Thread.sleep(1000);
                    logSuccessResponse();
                } catch (Exception e) {
                    logFailedResponse();
                } finally {
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                }
                return null;
            });
        } catch (Exception e) {
            logFailedResponse();
            System.out.printf("processRequest() - exception with message: %s\n", e.getMessage());
        }
    }

    // Get Task Result
    public boolean isTaskComplete(String taskId) {
        Future<?> future = taskMap.get(taskId);
        return future != null && future.isDone();
    }

    private void cleanUpCompletedTasks() {
        System.out.println("Clean up started");
        Set<String> taskIds = taskMap.keySet();
        for (String taskId : taskIds) {
            if (isTaskComplete(taskId)) {
                taskMap.remove(taskId);
                System.out.printf("Task with id = %s is deleted\n", taskId);
            }
        }
        System.out.println("Clean up ended");
    }

    public void shutdown() {
        executor.shutdown();
        cleanupScheduler.shutdown();
    }
}