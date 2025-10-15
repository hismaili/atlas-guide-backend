package com.smarttours.atlasguidebackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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

}
