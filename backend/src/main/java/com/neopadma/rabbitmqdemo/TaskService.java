
package com.neopadma.rabbitmqdemo;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private static final int MAX_ATTEMPTS = 3;

    private final RabbitTemplate rabbitTemplate;
    private final TaskRepository repository;

    // Demo-only counters for observing retry attempts
    private final Map<String, AtomicInteger> retryAttempts =
            new ConcurrentHashMap<>();

    public TaskService(
            RabbitTemplate rabbitTemplate,
            TaskRepository repository) {

        this.rabbitTemplate = rabbitTemplate;
        this.repository = repository;
    }

    public record Task(
            String id,
            String text,
            String status,
            String result
    ) {}

    private static Task toTask(TaskJob job) {
        return new Task(
                job.getId(),
                job.getText(),
                job.getStatus(),
                job.getResult()
        );
    }

    // Producer: Save the task and publish its ID
    public Task submit(String text) {

        String id = UUID.randomUUID().toString();

        TaskJob job = new TaskJob(id, text);

        repository.saveAndFlush(job);

        rabbitTemplate.convertAndSend(
                RabbitConfig.QUEUE_NAME, id
        );

        return toTask(job);
    }

    public Task find(String id) {
        return repository.findById(id)
                .map(TaskService::toTask)
                .orElse(null);
    }

    
    public List<Task> findAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(TaskService::toTask)
                .toList();
    }


    // Consumer: Process tasks with retry support
    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void process(String taskId)
            throws InterruptedException {

        if (taskId == null || taskId.isBlank()) {
            System.out.println("Ignored empty RabbitMQ message");
            return;
        }

        TaskJob job = repository.findById(taskId)
                .orElse(null);

        if (job == null) {
            System.out.println("Task not found: " + taskId);
            return;
        }

        // Ignore tasks already completed
        if ("COMPLETED".equals(job.getStatus())) {
            retryAttempts.remove(taskId);
            System.out.println(
                    "Task already completed: " + taskId
            );
            return;
        }

        // Count this processing attempt
        int attempt = retryAttempts
                .computeIfAbsent(
                        taskId,
                        id -> new AtomicInteger(0)
                )
                .incrementAndGet();

        System.out.println(
                "Processing task: " + taskId
                + " | Attempt " + attempt
                + "/" + MAX_ATTEMPTS
        );

        job.setStatus("PROCESSING");
        repository.saveAndFlush(job);

        // Simulate background processing
        Thread.sleep(5000);

        // FAIL: Always fails
        boolean permanentFailure =
                job.getText().startsWith("FAIL:");

        // FLAKY: Fails on attempts 1 and 2 only
        boolean temporaryFailure =
                job.getText().startsWith("FLAKY:")
                && attempt < MAX_ATTEMPTS;

        if (permanentFailure || temporaryFailure) {

            boolean finalAttempt = attempt >= MAX_ATTEMPTS;

            if (finalAttempt) {

                job.setStatus("FAILED");
                job.setResult(
                        "Failed after " + attempt + " attempts"
                );

                retryAttempts.remove(taskId);

            } else {

                job.setStatus("RETRYING");
                job.setResult(
                        "Simulated error on attempt " + attempt
                );
            }

            repository.saveAndFlush(job);

            System.out.println(
                    "Task failed: " + taskId
                    + " | Attempt " + attempt
            );

            // Spring AMQP sees the exception and retries
            throw new IllegalStateException(
                    "Simulated processing failure"
            );
        }

        // Successful processing
        int wordCount =
                job.getText().trim().split("\\s+").length;

        job.setStatus("COMPLETED");
        job.setResult("Word count: " + wordCount);

        repository.saveAndFlush(job);

        retryAttempts.remove(taskId);

        System.out.println(
                "Completed task: " + taskId
                + " | Attempt " + attempt
        );
    }
}
