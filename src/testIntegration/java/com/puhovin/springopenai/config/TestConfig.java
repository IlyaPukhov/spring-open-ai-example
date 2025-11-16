package com.puhovin.springopenai.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public OpenAiChatModel openAiChatModel() {
        return mock(OpenAiChatModel.class);
    }
}
