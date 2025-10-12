package com.smarttours.atlasguidebackend.infrastructure.configs;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"gemini"})
public class GeminiChatClientConfiguration {

    @Bean
    public ChatClient geminiChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }
}
