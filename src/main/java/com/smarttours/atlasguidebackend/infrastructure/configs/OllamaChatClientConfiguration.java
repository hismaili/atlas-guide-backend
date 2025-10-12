//package com.smarttours.atlasguidebackend.infrastructure.configs;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.prompt.ChatOptions;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.context.annotation.Profile;
//
//@Configuration
//@Profile({"!gemini", "ollama"})
//public class OllamaChatClientConfiguration {
//
//    @Bean
//    @Primary
//    public ChatClient chatClientBuilder(ChatClient.Builder chatClientBuilder) {
//        ChatClient.Builder builder = chatClientBuilder.defaultOptions(ChatOptions.builder()
//                .model("gemma3:4b")
//                .build());
//        return builder.build();
//    }
//}
