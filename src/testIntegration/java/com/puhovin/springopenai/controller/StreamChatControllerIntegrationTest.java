package com.puhovin.springopenai.controller;

import com.puhovin.springopenai.config.TestConfig;
import com.puhovin.springopenai.dto.ChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@Import(TestConfig.class)
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("StreamChatController Integration Tests")
class StreamChatControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OpenAiChatModel chatModel;

    @BeforeEach
    void setup() {
        reset(chatModel);
    }

    @Test
    @DisplayName("POST /chat/stream should stream chat response successfully")
    void shouldStreamChatResponse_successfully() {
        ChatRequest request = new ChatRequest("Hello, chat!");
        ChatResponse response1 = createChatResponse("Hello");
        ChatResponse response2 = createChatResponse("world");
        ChatResponse response3 = createChatResponse("test");

        Flux<ChatResponse> chatResponseFlux = Flux.just(response1, response2, response3);
        doReturn(chatResponseFlux).when(chatModel).stream(any(Prompt.class));

        Flux<String> result = webTestClient.post()
                .uri("/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(result)
                .expectNext("Hello")
                .expectNext("world")
                .expectNext("test")
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        verify(chatModel).stream(any(Prompt.class));
    }

    @Test
    @DisplayName("POST /chat/stream should return 400 for blank message")
    void shouldReturn400_forBlankMessage() {
        ChatRequest request = new ChatRequest("");

        webTestClient.post()
                .uri("/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("POST /chat/stream should return 400 for null message")
    void shouldReturn400_forNullMessage() {
        webTestClient.post()
                .uri("/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\": null}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("POST /chat/stream-sse should stream SSE events successfully")
    void shouldStreamSseEvents_successfully() {
        ChatRequest request = new ChatRequest("Test message");
        ChatResponse response1 = createChatResponse("Chunk1");
        ChatResponse response2 = createChatResponse("Chunk2");

        Flux<ChatResponse> chatResponseFlux = Flux.just(response1, response2);
        doReturn(chatResponseFlux).when(chatModel).stream(any(Prompt.class));

        Flux<String> result = webTestClient.post()
                .uri("/chat/stream-sse")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(result)
                .expectNext("Chunk1")
                .expectNext("Chunk2")
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        verify(chatModel).stream(any(Prompt.class));
    }

    @Test
    @DisplayName("POST /chat/stream-sse should include event IDs in SSE response")
    void shouldIncludeEventIds_inSseResponse() {
        ChatRequest request = new ChatRequest("Test");
        ChatResponse response = createChatResponse("Response");

        Flux<ChatResponse> chatResponseFlux = Flux.just(response);
        doReturn(chatResponseFlux).when(chatModel).stream(any(Prompt.class));

        Flux<String> result = webTestClient.post()
                .uri("/chat/stream-sse")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(result)
                .expectNext("Response")
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        verify(chatModel).stream(any(Prompt.class));
    }

    @Test
    @DisplayName("POST /chat/stream should filter out null content")
    void shouldFilterOutNullContent_inStream() {
        ChatRequest request = new ChatRequest("Test");
        ChatResponse responseWithContent = createChatResponse("Valid");
        ChatResponse responseWithNull = createChatResponse(null);
        ChatResponse responseWithContent2 = createChatResponse("Content");

        Flux<ChatResponse> chatResponseFlux = Flux.just(
                responseWithContent,
                responseWithNull,
                responseWithContent2
        );
        doReturn(chatResponseFlux).when(chatModel).stream(any(Prompt.class));

        Flux<String> result = webTestClient.post()
                .uri("/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(result)
                .expectNext("Valid")
                .expectNext("Content")
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        verify(chatModel).stream(any(Prompt.class));
    }

    @Test
    @DisplayName("POST /chat/stream should handle empty response stream")
    void shouldHandleEmptyResponseStream() {
        ChatRequest request = new ChatRequest("Test");
        Flux<ChatResponse> emptyFlux = Flux.empty();
        doReturn(emptyFlux).when(chatModel).stream(any(Prompt.class));

        Flux<String> result = webTestClient.post()
                .uri("/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(result)
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        verify(chatModel).stream(any(Prompt.class));
    }

    @Test
    @DisplayName("POST /chat should return complete response successfully (non-streaming)")
    void shouldReturnCompleteResponse_successfully() {
        ChatRequest request = new ChatRequest("Hello, chat!");
        ChatResponse response = createChatResponse("Hello! How can I help you today?");

        doReturn(response).when(chatModel).call(any(Prompt.class));

        webTestClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .isEqualTo("Hello! How can I help you today?");

        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    @DisplayName("POST /chat should return 400 for blank message (non-streaming)")
    void shouldReturn400_forBlankMessage_blocking() {
        ChatRequest request = new ChatRequest("");

        webTestClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("POST /chat should return 400 for null message (non-streaming)")
    void shouldReturn400_forNullMessage_blocking() {
        webTestClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\": null}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("POST /chat should return empty string when content is null (non-streaming)")
    void shouldReturnEmptyString_whenContentIsNull() {
        ChatRequest request = new ChatRequest("Test message");
        ChatResponse response = createChatResponse(null);

        doReturn(response).when(chatModel).call(any(Prompt.class));

        webTestClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .isEqualTo("");

        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    @DisplayName("POST /chat should handle long response correctly (non-streaming)")
    void shouldHandleLongResponse_correctly() {
        ChatRequest request = new ChatRequest("Tell me a long story");
        String longResponse = "This is a very long response. ".repeat(50);
        ChatResponse response = createChatResponse(longResponse);

        doReturn(response).when(chatModel).call(any(Prompt.class));

        webTestClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .isEqualTo(longResponse);

        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    @DisplayName("POST /chat should handle special characters in response (non-streaming)")
    void shouldHandleSpecialCharacters_inResponse() {
        ChatRequest request = new ChatRequest("Special chars");
        String responseWithSpecialChars = "Response with special chars: !@#$%^&*()_+-=[]{}|;':\"<>?,./";
        ChatResponse response = createChatResponse(responseWithSpecialChars);

        doReturn(response).when(chatModel).call(any(Prompt.class));

        webTestClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .isEqualTo(responseWithSpecialChars);

        verify(chatModel).call(any(Prompt.class));
    }

    private ChatResponse createChatResponse(String text) {
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage output = mock(AssistantMessage.class);

        doReturn(text).when(output).getText();
        doReturn(output).when(generation).getOutput();
        doReturn(generation).when(chatResponse).getResult();

        return chatResponse;
    }
}
