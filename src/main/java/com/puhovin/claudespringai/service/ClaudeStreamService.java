package com.puhovin.claudespringai.service;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ClaudeStreamService {

    private final AnthropicChatModel chatModel;

    public ClaudeStreamService(AnthropicChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public Flux<String> streamChat(String message) {
        Prompt prompt = new Prompt(message);

        return chatModel.stream(prompt)
                .mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText())
                .filter(content -> content != null && !content.isEmpty());
    }
}
