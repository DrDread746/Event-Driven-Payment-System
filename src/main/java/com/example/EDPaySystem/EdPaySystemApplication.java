package com.example.EDPaySystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EdPaySystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdPaySystemApplication.class, args);
	}

}
