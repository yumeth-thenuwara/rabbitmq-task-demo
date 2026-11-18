
package com.neopadma.rabbitmqdemo;

import java.util.List;
import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final RabbitTemplate rabbitTemplate;
    private final TaskRepository repository;

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

    // Producer: Save task, then publish its ID
    public Task submit(String text) {
        String id = UUID.randomUUID().toString();

        TaskJob job = new TaskJob(id, text);

        // Save before publishing to prevent a race
        // where the consumer cannot find the job.
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
        return repository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
        ).stream().map(TaskService::toTask).toList();
    }

    // Consumer: Receive a message and process it
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

        // Basic protection against completed messages
        if ("COMPLETED".equals(job.getStatus())) {
            System.out.println("Task already completed: " + taskId);
            return;
        }

        job.setStatus("PROCESSING");
        repository.saveAndFlush(job);

        System.out.println("Processing task: " + taskId);

        
        // Simulate a background operation
        Thread.sleep(5000);

        // Simulate a processing failure for testing
        if (job.getText().startsWith("FAIL:")) {

            job.setStatus("FAILED");
            job.setResult("Simulated processing failure");

            // Save failure status to PostgreSQL
            repository.saveAndFlush(job);

            System.out.println(
                    "Simulated task failure: " + taskId
            );

            // Throw an exception so RabbitMQ rejects
            // and dead-letters the message
            throw new IllegalStateException(
                    "Simulated processing failure for task " + taskId
            );
        }

        // Normal word counting continues here
        int wordCount =
                job.getText().trim().split("\\s+").length;


        job.setStatus("COMPLETED");
        job.setResult("Word count: " + wordCount);

        repository.saveAndFlush(job);

        System.out.println("Completed task: " + taskId);
    }
}
