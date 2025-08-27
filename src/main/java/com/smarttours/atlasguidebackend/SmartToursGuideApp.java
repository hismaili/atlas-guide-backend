package com.smarttours.atlasguidebackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@SpringBootApplication
@EnableAsync
public class SmartToursGuideApp {

	public static void main(String[] args) {
		SpringApplication.run(SmartToursGuideApp.class, args);
	}

/*	@Bean
	public ChatClient gemma3ChatClient(ChatClient.Builder chatClientBuilder) {
		return chatClientBuilder
				.defaultOptions(ChatOptions.builder()
						.model("gemma3:4b")
						.build())
				.build();
	}

	@Bean
	public ChatClient granite3ChatClient(ChatClient.Builder chatClientBuilder) {
		return chatClientBuilder
				.defaultOptions(ChatOptions.builder()
						.model("granite3.3:2b")
						.build())
				.build();
	}

	@Bean
	ObjectMapper customObjectMapper() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		return new ObjectMapper()
				.findAndRegisterModules()
				.setDefaultPropertyInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
				.setDateFormat(dateFormat);
	}*/

}
