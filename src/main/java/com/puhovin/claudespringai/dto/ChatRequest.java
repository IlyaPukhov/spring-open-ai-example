package com.puhovin.claudespringai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Chat request containing a message to be sent to Claude AI")
public record ChatRequest(
        @Schema(description = "The message content to send to Claude AI", example = "Hello, Claude!")
        @NotBlank(message = "must not be blank")
        String message
) {}
