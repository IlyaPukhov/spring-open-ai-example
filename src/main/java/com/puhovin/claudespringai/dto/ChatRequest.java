package com.puhovin.claudespringai.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank String message
) {}
