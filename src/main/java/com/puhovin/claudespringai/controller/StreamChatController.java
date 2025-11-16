package com.puhovin.claudespringai.controller;

import com.puhovin.claudespringai.dto.ChatRequest;
import com.puhovin.claudespringai.service.ClaudeStreamService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class StreamChatController {

    private final ClaudeStreamService streamService;

    public StreamChatController(ClaudeStreamService streamService) {
        this.streamService = streamService;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody @Validated ChatRequest request) {
        return streamService.streamChat(request.message());
    }

    @PostMapping(value = "/stream-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChatSse(@RequestBody @Validated ChatRequest request) {
        return streamService.streamChat(request.message())
                .index()
                .map(tuple -> ServerSentEvent.<String>builder()
                        .id(String.valueOf(tuple.getT1()))
                        .event("chat.message")
                        .data(tuple.getT2())
                        .build());
    }
}