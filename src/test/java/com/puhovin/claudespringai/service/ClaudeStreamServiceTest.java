package com.puhovin.claudespringai.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClaudeStreamService Unit Tests")
class ClaudeStreamServiceTest {

    @Mock
    private AnthropicChatModel chatModel;

    @InjectMocks
    private ClaudeStreamService service;

    @Nested
    @DisplayName("streamChat() tests")
    class StreamChatTests {

        @Test
        @DisplayName("Should stream chat response when valid message is provided")
        void shouldStreamChatResponse_whenValidMessageProvided() {
            String message = "Hello, Claude!";
            ChatResponse response1 = createChatResponse("Hello");
            ChatResponse response2 = createChatResponse(" there");
            ChatResponse response3 = createChatResponse("!");

            Flux<ChatResponse> chatResponseFlux = Flux.just(response1, response2, response3);
            doReturn(chatResponseFlux).when(chatModel).stream(any(Prompt.class));

            Flux<String> result = service.streamChat(message);

            StepVerifier.create(result)
                    .expectNext("Hello")
                    .expectNext(" there")
                    .expectNext("!")
                    .verifyComplete();

            ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
            verify(chatModel).stream(promptCaptor.capture());

            Prompt capturedPrompt = promptCaptor.getValue();
            assertThat(capturedPrompt).isNotNull();
            assertThat(capturedPrompt.getContents()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should filter out null content from chat response")
        void shouldFilterOutNullContent_fromChatResponse() {
            String message = "Test message";
            ChatResponse responseWithContent = createChatResponse("Valid content");
            ChatResponse responseWithNull = createChatResponse(null);
            ChatResponse responseWithContent2 = createChatResponse("Another valid");

            Flux<ChatResponse> chatResponseFlux = Flux.just(
                    responseWithContent,
                    responseWithNull,
                    responseWithContent2
            );
            doReturn(chatResponseFlux).when(chatModel).stream(any(Prompt.class));

            Flux<String> result = service.streamChat(message);

            StepVerifier.create(result)
                    .expectNext("Valid content")
                    .expectNext("Another valid")
                    .verifyComplete();

            verify(chatModel).stream(any(Prompt.class));
        }

        @Test
        @DisplayName("Should filter out empty content from chat response")
        void shouldFilterOutEmptyContent_fromChatResponse() {
            String message = "Test message";
            ChatResponse responseWithContent = createChatResponse("Valid content");
            ChatResponse responseWithEmpty = createChatResponse("");
            ChatResponse responseWithContent2 = createChatResponse("Another valid");

            Flux<ChatResponse> chatResponseFlux = Flux.just(
                    responseWithContent,
                    responseWithEmpty,
                    responseWithContent2
            );
            doReturn(chatResponseFlux).when(chatModel).stream(any(Prompt.class));

            Flux<String> result = service.streamChat(message);

            StepVerifier.create(result)
                    .expectNext("Valid content")
                    .expectNext("Another valid")
                    .verifyComplete();

            verify(chatModel).stream(any(Prompt.class));
        }

        @Test
        @DisplayName("Should return empty flux when all responses have null or empty content")
        void shouldReturnEmptyFlux_whenAllResponsesHaveNullOrEmptyContent() {
            String message = "Test message";
            ChatResponse responseWithNull = createChatResponse(null);
            ChatResponse responseWithEmpty = createChatResponse("");

            Flux<ChatResponse> chatResponseFlux = Flux.just(responseWithNull, responseWithEmpty);
            doReturn(chatResponseFlux).when(chatModel).stream(any(Prompt.class));

            Flux<String> result = service.streamChat(message);

            StepVerifier.create(result)
                    .verifyComplete();

            verify(chatModel).stream(any(Prompt.class));
        }

        @Test
        @DisplayName("Should propagate error when chat model throws exception")
        void shouldPropagateError_whenChatModelThrowsException() {
            String message = "Error message";
            RuntimeException expectedException = new RuntimeException("Chat model error");
            doReturn(Flux.error(expectedException)).when(chatModel).stream(any(Prompt.class));

            Flux<String> result = service.streamChat(message);

            StepVerifier.create(result)
                    .expectErrorMatches(throwable ->
                            throwable instanceof RuntimeException &&
                            throwable.getMessage().equals("Chat model error"))
                    .verify();

            verify(chatModel).stream(any(Prompt.class));
        }

        @Test
        @DisplayName("Should handle empty message and create prompt correctly")
        void shouldHandleEmptyMessage_andCreatePromptCorrectly() {
            String emptyMessage = "";
            ChatResponse response = createChatResponse("Response to empty");

            Flux<ChatResponse> chatResponseFlux = Flux.just(response);
            doReturn(chatResponseFlux).when(chatModel).stream(any(Prompt.class));

            Flux<String> result = service.streamChat(emptyMessage);

            StepVerifier.create(result)
                    .expectNext("Response to empty")
                    .verifyComplete();

            ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
            verify(chatModel).stream(promptCaptor.capture());

            Prompt capturedPrompt = promptCaptor.getValue();
            assertThat(capturedPrompt.getContents()).isEqualTo(emptyMessage);
        }

        @Test
        @DisplayName("Should handle multiple rapid responses correctly")
        void shouldHandleMultipleRapidResponses_correctly() {
            String message = "Rapid test";
            Flux<ChatResponse> rapidResponses = Flux.range(1, 10)
                    .map(i -> createChatResponse("Chunk " + i));
            doReturn(rapidResponses).when(chatModel).stream(any(Prompt.class));

            Flux<String> result = service.streamChat(message);

            StepVerifier.create(result)
                    .expectNext("Chunk 1")
                    .expectNext("Chunk 2")
                    .expectNext("Chunk 3")
                    .expectNext("Chunk 4")
                    .expectNext("Chunk 5")
                    .expectNext("Chunk 6")
                    .expectNext("Chunk 7")
                    .expectNext("Chunk 8")
                    .expectNext("Chunk 9")
                    .expectNext("Chunk 10")
                    .verifyComplete();

            verify(chatModel).stream(any(Prompt.class));
        }
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
