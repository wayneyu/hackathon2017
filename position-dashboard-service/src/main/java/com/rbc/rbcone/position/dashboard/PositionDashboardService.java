package com.rbc.rbcone.position.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class PositionDashboardService {
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(PositionDashboardService.class, args);
	}

}