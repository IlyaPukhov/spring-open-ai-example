package com.puhovin.springopenai.service;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class OpenAiStreamService {

    private final OpenAiChatModel chatModel;

    public OpenAiStreamService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public Flux<String> streamChat(String message) {
        Prompt prompt = new Prompt(message);

        return chatModel.stream(prompt)
                .mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText())
                .filter(content -> content != null && !content.isEmpty());
    }
}
