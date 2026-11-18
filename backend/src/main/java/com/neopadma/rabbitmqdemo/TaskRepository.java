
package com.neopadma.rabbitmqdemo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository
        extends JpaRepository<TaskJob, String> {

    List<TaskJob> findAllByOrderByCreatedAtDesc();
}
