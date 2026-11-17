package com.neopadma.rabbitmqdemo;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final RabbitTemplate rabbitTemplate;
    private final Map<String, Task> tasks =
            new ConcurrentHashMap<>();

    public TaskService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public record Task(
            String id,
            String text,
            String status,
            String result
    ) {}

    // Producer: Sends a message to RabbitMQ
    public Task submit(String text) {
        String id = UUID.randomUUID().toString();

        Task task = new Task(id, text, "QUEUED", null);
        tasks.put(id, task);

        rabbitTemplate.convertAndSend(
                RabbitConfig.QUEUE_NAME, id
        );

        return task;
    }

    public Task find(String id) {
        return tasks.get(id);
    }

    // Consumer: Receives a message from RabbitMQ
    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void process(String taskId) throws InterruptedException {

        // Ignore empty RabbitMQ messages
        if (taskId == null || taskId.isBlank()) {
            System.out.println("Ignored empty RabbitMQ message");
            return;
        }

        Task task = tasks.get(taskId);

        if (task == null) {
            System.out.println("Task not found: " + taskId);
            return;
        }

        tasks.put(taskId,
                new Task(taskId, task.text(), "PROCESSING", null));

        System.out.println("Processing task: " + taskId);

        // Simulate a slow background operation
        Thread.sleep(5000);

        int wordCount = task.text().trim().split("\\s+").length;

        String result = "Word count: " + wordCount;

        tasks.put(taskId,
                new Task(taskId, task.text(), "COMPLETED", result));

        System.out.println("Completed task: " + taskId);
    }
}