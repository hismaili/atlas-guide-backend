package com.smarttours;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableAsync
@EnableWebSecurity
public class SmartToursGuideApp {

	public static void main(String[] args) {
		SpringApplication.run(SmartToursGuideApp.class, args);
	}

}
