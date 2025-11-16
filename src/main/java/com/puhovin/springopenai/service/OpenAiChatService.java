package com.puhovin.springopenai.service;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class OpenAiChatService {

    private final OpenAiChatModel chatModel;

    public OpenAiChatService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public Mono<String> chat(String message) {
        return Mono.fromCallable(() -> {
            Prompt prompt = new Prompt(message);
            var chatResponse = chatModel.call(prompt);
            String content = chatResponse.getResult().getOutput().getText();
            return content != null ? content : "";
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
