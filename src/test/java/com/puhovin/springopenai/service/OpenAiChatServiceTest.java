package com.puhovin.springopenai.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAiChatService Unit Tests")
class OpenAiChatServiceTest {

    @Mock
    private OpenAiChatModel chatModel;

    @InjectMocks
    private OpenAiChatService service;

    @Nested
    @DisplayName("chat() tests")
    class ChatTests {

        @Test
        @DisplayName("Should return chat response when valid message is provided")
        void shouldReturnChatResponse_whenValidMessageProvided() {
            String message = "Hello, chat!";
            String expectedResponse = "Hello! How can I help you today?";
            ChatResponse response = createChatResponse(expectedResponse);

            doReturn(response).when(chatModel).call(any(Prompt.class));

            Mono<String> result = service.chat(message);

            StepVerifier.create(result)
                    .expectNext(expectedResponse)
                    .verifyComplete();

            ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
            verify(chatModel).call(promptCaptor.capture());

            Prompt capturedPrompt = promptCaptor.getValue();
            assertThat(capturedPrompt).isNotNull();
            assertThat(capturedPrompt.getContents()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should return empty string when content is null")
        void shouldReturnEmptyString_whenContentIsNull() {
            String message = "Test message";
            ChatResponse response = createChatResponse(null);

            doReturn(response).when(chatModel).call(any(Prompt.class));

            Mono<String> result = service.chat(message);

            StepVerifier.create(result)
                    .expectNext("")
                    .verifyComplete();

            verify(chatModel).call(any(Prompt.class));
        }

        @Test
        @DisplayName("Should return content when content is empty string")
        void shouldReturnEmptyString_whenContentIsEmptyString() {
            String message = "Test message";
            ChatResponse response = createChatResponse("");

            doReturn(response).when(chatModel).call(any(Prompt.class));

            Mono<String> result = service.chat(message);

            StepVerifier.create(result)
                    .expectNext("")
                    .verifyComplete();

            verify(chatModel).call(any(Prompt.class));
        }

        @Test
        @DisplayName("Should throw exception when chat model throws exception")
        void shouldThrowException_whenChatModelThrowsException() {
            String message = "Error message";
            RuntimeException expectedException = new RuntimeException("Chat model error");
            doThrow(expectedException).when(chatModel).call(any(Prompt.class));

            Mono<String> result = service.chat(message);

            StepVerifier.create(result)
                    .expectErrorMatches(throwable ->
                            throwable instanceof RuntimeException &&
                            throwable.getMessage().equals("Chat model error"))
                    .verify();

            verify(chatModel).call(any(Prompt.class));
        }

        @Test
        @DisplayName("Should handle empty message and create prompt correctly")
        void shouldHandleEmptyMessage_andCreatePromptCorrectly() {
            String emptyMessage = "";
            String expectedResponse = "Response to empty";
            ChatResponse response = createChatResponse(expectedResponse);

            doReturn(response).when(chatModel).call(any(Prompt.class));

            Mono<String> result = service.chat(emptyMessage);

            StepVerifier.create(result)
                    .expectNext(expectedResponse)
                    .verifyComplete();

            ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
            verify(chatModel).call(promptCaptor.capture());

            Prompt capturedPrompt = promptCaptor.getValue();
            assertThat(capturedPrompt.getContents()).isEqualTo(emptyMessage);
        }

        @Test
        @DisplayName("Should handle long message correctly")
        void shouldHandleLongMessage_correctly() {
            String longMessage = "Lorem ipsum ".repeat(100);
            String expectedResponse = "Processed long message";
            ChatResponse response = createChatResponse(expectedResponse);

            doReturn(response).when(chatModel).call(any(Prompt.class));

            Mono<String> result = service.chat(longMessage);

            StepVerifier.create(result)
                    .expectNext(expectedResponse)
                    .verifyComplete();

            verify(chatModel).call(any(Prompt.class));
        }

        @Test
        @DisplayName("Should handle special characters in message")
        void shouldHandleSpecialCharacters_inMessage() {
            String messageWithSpecialChars = "Test with special chars: !@#$%^&*()_+-=[]{}|;':\"<>?,./";
            String expectedResponse = "Response to special chars";
            ChatResponse response = createChatResponse(expectedResponse);

            doReturn(response).when(chatModel).call(any(Prompt.class));

            Mono<String> result = service.chat(messageWithSpecialChars);

            StepVerifier.create(result)
                    .expectNext(expectedResponse)
                    .verifyComplete();

            ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
            verify(chatModel).call(promptCaptor.capture());

            Prompt capturedPrompt = promptCaptor.getValue();
            assertThat(capturedPrompt.getContents()).isEqualTo(messageWithSpecialChars);
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
