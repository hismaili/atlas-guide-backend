package com.smarttours.atlasguidebackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@SpringBootApplication
@EnableAsync
@EnableWebSecurity
public class SmartToursGuideApp {

	public static void main(String[] args) {
		SpringApplication.run(SmartToursGuideApp.class, args);
	}

	@Bean
	public CommandLineRunner debugEnv(Environment env) {
		System.out.println("===============     Debugging Environment Variables...");
		return args -> {
			System.out.println("=== Environment Debug ===");
			System.out.println("VAULT_ROLE_ID: " + env.getProperty("VAULT_ROLE_ID"));
			System.out.println("VAULT_SECRET_ID: " + (env.getProperty("VAULT_SECRET_ID") != null ? "****" : "MISSING"));
			System.out.println("Full Vault URI: " + env.getProperty("spring.cloud.vault.uri"));
			System.out.println("Full Vault URI: " + env.getProperty("api-key"));
			System.out.println("=========================");
		};
	}

}
