
package com.neopadma.rabbitmqdemo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    public record TaskRequest(String text) {}

    @PostMapping
    public TaskService.Task create(
            @RequestBody TaskRequest request) {

        if (request == null
                || request.text() == null
                || request.text().isBlank()
                || request.text().length() > 2000) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Text must contain 1-2000 characters"
            );
        }

        return service.submit(request.text());
    }

    @GetMapping
    public List<TaskService.Task> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TaskService.Task get(@PathVariable String id) {

        TaskService.Task task = service.find(id);

        if (task == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Task not found"
            );
        }

        return task;
    }
}
