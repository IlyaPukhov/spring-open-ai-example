package com.puhovin.claudespringai.controller;

import com.puhovin.claudespringai.dto.ChatRequest;
import com.puhovin.claudespringai.service.ClaudeStreamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("StreamChatController Unit Tests")
class StreamChatControllerTest {

    @Mock
    private ClaudeStreamService streamService;

    @InjectMocks
    private StreamChatController controller;

    @Nested
    @DisplayName("streamChat() tests")
    class StreamChatTests {

        @Test
        @DisplayName("Should return streaming response when valid request is provided")
        void shouldReturnStreamingResponse_whenValidRequestProvided() {
            ChatRequest request = new ChatRequest("Hello, Claude!");
            Flux<String> expectedResponse = Flux.just("Hello", " there", "!");
            doReturn(expectedResponse).when(streamService).streamChat(request.message());

            Flux<String> actualResponse = controller.streamChat(request);

            StepVerifier.create(actualResponse)
                    .expectNext("Hello")
                    .expectNext(" there")
                    .expectNext("!")
                    .verifyComplete();

            verify(streamService).streamChat(request.message());
        }

        @Test
        @DisplayName("Should return empty flux when service returns empty stream")
        void shouldReturnEmptyFlux_whenServiceReturnsEmptyStream() {
            ChatRequest request = new ChatRequest("Empty query");
            Flux<String> emptyResponse = Flux.empty();
            doReturn(emptyResponse).when(streamService).streamChat(request.message());

            Flux<String> actualResponse = controller.streamChat(request);

            StepVerifier.create(actualResponse)
                    .verifyComplete();

            verify(streamService).streamChat(request.message());
        }

        @Test
        @DisplayName("Should propagate error when service throws exception")
        void shouldPropagateError_whenServiceThrowsException() {
            ChatRequest request = new ChatRequest("Error query");
            RuntimeException expectedException = new RuntimeException("Service error");
            doReturn(Flux.error(expectedException)).when(streamService).streamChat(request.message());

            Flux<String> actualResponse = controller.streamChat(request);

            StepVerifier.create(actualResponse)
                    .expectErrorMatches(throwable ->
                            throwable instanceof RuntimeException &&
                            throwable.getMessage().equals("Service error"))
                    .verify();

            verify(streamService).streamChat(request.message());
        }
    }

    @Nested
    @DisplayName("streamChatSse() tests")
    class StreamChatSseTests {

        @Test
        @DisplayName("Should return SSE with sequential IDs and event type")
        void shouldReturnSseWithSequentialIds_whenValidRequestProvided() {
            ChatRequest request = new ChatRequest("Hello, Claude!");
            Flux<String> serviceResponse = Flux.just("Hello", " there", "!");
            doReturn(serviceResponse).when(streamService).streamChat(request.message());

            Flux<ServerSentEvent<String>> actualResponse = controller.streamChatSse(request);

            StepVerifier.create(actualResponse)
                    .assertNext(event -> {
                        assertThat(event.id()).isEqualTo("0");
                        assertThat(event.event()).isEqualTo("chat.message");
                        assertThat(event.data()).isEqualTo("Hello");
                    })
                    .assertNext(event -> {
                        assertThat(event.id()).isEqualTo("1");
                        assertThat(event.event()).isEqualTo("chat.message");
                        assertThat(event.data()).isEqualTo(" there");
                    })
                    .assertNext(event -> {
                        assertThat(event.id()).isEqualTo("2");
                        assertThat(event.event()).isEqualTo("chat.message");
                        assertThat(event.data()).isEqualTo("!");
                    })
                    .verifyComplete();

            verify(streamService).streamChat(request.message());
        }

        @Test
        @DisplayName("Should return empty SSE flux when service returns empty stream")
        void shouldReturnEmptySseFlux_whenServiceReturnsEmptyStream() {
            ChatRequest request = new ChatRequest("Empty query");
            Flux<String> emptyResponse = Flux.empty();
            doReturn(emptyResponse).when(streamService).streamChat(request.message());

            Flux<ServerSentEvent<String>> actualResponse = controller.streamChatSse(request);

            StepVerifier.create(actualResponse)
                    .verifyComplete();

            verify(streamService).streamChat(request.message());
        }

        @Test
        @DisplayName("Should propagate error when service throws exception in SSE mode")
        void shouldPropagateError_whenServiceThrowsExceptionInSseMode() {
            ChatRequest request = new ChatRequest("Error query");
            RuntimeException expectedException = new RuntimeException("SSE service error");
            doReturn(Flux.error(expectedException)).when(streamService).streamChat(request.message());

            Flux<ServerSentEvent<String>> actualResponse = controller.streamChatSse(request);

            StepVerifier.create(actualResponse)
                    .expectErrorMatches(throwable ->
                            throwable instanceof RuntimeException &&
                            throwable.getMessage().equals("SSE service error"))
                    .verify();

            verify(streamService).streamChat(request.message());
        }

        @Test
        @DisplayName("Should handle single message with correct SSE structure")
        void shouldHandleSingleMessage_withCorrectSseStructure() {
            ChatRequest request = new ChatRequest("Single message");
            Flux<String> singleMessageResponse = Flux.just("Single response");
            doReturn(singleMessageResponse).when(streamService).streamChat(request.message());

            Flux<ServerSentEvent<String>> actualResponse = controller.streamChatSse(request);

            StepVerifier.create(actualResponse)
                    .assertNext(event -> {
                        assertThat(event.id()).isEqualTo("0");
                        assertThat(event.event()).isEqualTo("chat.message");
                        assertThat(event.data()).isEqualTo("Single response");
                    })
                    .verifyComplete();

            verify(streamService).streamChat(request.message());
        }
    }
}
