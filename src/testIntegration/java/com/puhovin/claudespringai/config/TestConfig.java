package com.puhovin.claudespringai.config;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public AnthropicChatModel anthropicChatModel() {
        return mock(AnthropicChatModel.class);
    }
}
