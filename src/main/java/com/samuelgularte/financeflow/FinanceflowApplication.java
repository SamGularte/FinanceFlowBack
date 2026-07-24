package com.samuelgularte.financeflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FinanceflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceflowApplication.class, args);
	}

}
