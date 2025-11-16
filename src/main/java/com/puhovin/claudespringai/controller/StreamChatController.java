package com.puhovin.claudespringai.controller;

import com.puhovin.claudespringai.dto.ChatRequest;
import com.puhovin.claudespringai.service.ClaudeStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Chat", description = "Streaming chat API for Claude AI interactions")
public class StreamChatController {

    private final ClaudeStreamService streamService;

    public StreamChatController(ClaudeStreamService streamService) {
        this.streamService = streamService;
    }

    @Operation(
            summary = "Stream chat response",
            description = "Sends a message to Claude AI and streams back the response as text chunks",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully streaming response",
                            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody @Validated ChatRequest request) {
        return streamService.streamChat(request.message());
    }

    @Operation(
            summary = "Stream chat response with Server-Sent Events",
            description = "Sends a message to Claude AI and streams back the response as structured Server-Sent Events with event IDs",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully streaming SSE response",
                            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
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