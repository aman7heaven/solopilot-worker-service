package com.autopilot.worker;

import com.autopilot.worker.notification.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootTest(properties = {
        "spring.rabbitmq.listener.auto-startup=false"
})
class WorkerServiceApplicationTests {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public EmailService emailService() {
            return new EmailService("dummy-key", "templateId", "", "", "", "");
        }
    }

    @Test
    void contextLoads() {
    }
}
