package com.smarttours.atlasguidebackend.infrastructure.configs;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile({"gemini"})
public class GeminiChatClientConfiguration {


    private Environment env;

    public GeminiChatClientConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    public ChatClient geminiChatClient(GoogleGenAiChatModel chatModel) {

        return ChatClient.create(chatModel);


    }
}
