
package com.neopadma.rabbitmqdemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        // In-memory database for this test
        "spring.datasource.url=jdbc:h2:mem:rabbitmqtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",

        // Do not connect to CloudAMQP during this test
        "spring.rabbitmq.addresses=amqp://guest:guest@127.0.0.1:5673",
        "spring.rabbitmq.dynamic=false",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring Boot starts with isolated test services.
    }
}
