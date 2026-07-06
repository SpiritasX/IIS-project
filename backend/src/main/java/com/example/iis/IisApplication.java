package com.example.iis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class IisApplication {

	public static void main(String[] args) {
		SpringApplication.run(IisApplication.class, args);
	}

}
