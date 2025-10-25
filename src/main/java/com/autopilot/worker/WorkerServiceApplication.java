package com.autopilot.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.autopilot", "com.autopilot.worker", "com.portfolio"})
@EntityScan(basePackages = {"com.autopilot", "com.autopilot.worker", "com.portfolio"})
public class  WorkerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkerServiceApplication.class, args);
	}

}
