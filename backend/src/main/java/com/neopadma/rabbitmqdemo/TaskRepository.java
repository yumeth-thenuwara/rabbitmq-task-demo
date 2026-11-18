
package com.neopadma.rabbitmqdemo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository
        extends JpaRepository<TaskJob, String> {
}
