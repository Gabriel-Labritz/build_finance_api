package com.gabriellabritz.build_finance_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BuildFinanceApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(BuildFinanceApiApplication.class, args);
	}
}
