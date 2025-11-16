package com.puhovin.claudespringai.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatRequest DTO Unit Validation tests")
class ChatRequestTest {

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    @DisplayName("Should pass validation when message is valid")
    void shouldPassValidation_whenMessageIsValid() {
        ChatRequest request = new ChatRequest("Hello, Claude!");

        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n\r"})
    @DisplayName("Should fail validation when message is blank")
    void shouldFailValidation_whenMessageIsBlank(String blankMessage) {
        ChatRequest request = new ChatRequest(blankMessage);

        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        ConstraintViolation<ChatRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).hasToString("message");
        assertThat(violation.getMessage()).hasToString("must not be blank");
    }

    @ParameterizedTest
    @MethodSource("provideValidMessages")
    @DisplayName("Should pass validation for various valid message formats")
    void shouldPassValidation_forVariousValidFormats(String validMessage) {
        ChatRequest request = new ChatRequest(validMessage);

        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    static Stream<String> provideValidMessages() {
        return Stream.of(
                "  Hello, Claude!  ",           // leading and trailing spaces with content
                "a",                                    // single character
                "a".repeat(10000),                // very long message
                "!@#$%^&*()_+-=[]{}|;':\",./<>?",       // special characters
                "Hello 世界 🌍 مرحبا"                   // Unicode characters
        );
    }
}
