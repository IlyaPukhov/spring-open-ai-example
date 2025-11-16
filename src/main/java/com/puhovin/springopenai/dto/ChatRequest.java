package com.puhovin.springopenai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Chat request containing a message to be sent to OpenAI")
public record ChatRequest(
        @Schema(description = "The message content to send to OpenAI", example = "Hello, chat!")
        @NotBlank(message = "must not be blank")
        String message
) {}
