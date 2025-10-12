package com.smarttours.atlasguidebackend.infrastructure.configs;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.fasterxml.jackson.core.StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_OPTIONALS;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION, true)
                .configure(REQUIRE_HANDLERS_FOR_JAVA8_OPTIONALS, false)
                .configure(REQUIRE_HANDLERS_FOR_JAVA8_TIMES, false)

                .build().registerModule(new JavaTimeModule());
    }
}
