package com.github.f1xman.statefun.tsukuyomi.dsl;

import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.nullValue;

class ExpectationsTest {

    @Test
    void throwsNullValueSpecExceptionIfNullIsPassed() {
        assertThatThrownBy(() -> Expectations.expectState(null, nullValue()))
                .isInstanceOf(NullValueSpecException.class)
                .hasMessage("ValueSpec of a verifying state is null");
    }

    @Test
    void throwsNullStateMatcherExceptionIfNullIsPassed() {
        assertThatThrownBy(() -> Expectations.expectState(ValueSpec.named("foo").withIntType(), null))
                .isInstanceOf(NullStateMatcherException.class)
                .hasMessage("Matcher cannot be null. Either use org.hamcrest.Matchers.*() or implement a custom one");
    }

    @Test
    void throwsNullExpectedEnvelopeExceptionIfNullIsPassed() {
        assertThatThrownBy(() -> Expectations.expectMessageInExactOrder(null))
                .isInstanceOf(NullExpectedEnvelopeException.class)
                .hasMessage("Expected envelope cannot be null. Use Envelope.builder() to build a message");
    }

    @Test
    void expectMessageInAnyOrderThrowsNullExpectedEnvelopeExceptionIfNullIsPassed() {
        assertThatThrownBy(() -> Expectations.expectMessageInAnyOrder(null))
                .isInstanceOf(NullExpectedEnvelopeException.class)
                .hasMessage("Expected envelope cannot be null. Use Envelope.builder() to build a message");
    }

    @Test
    void throwsNullExpectedEnvelopeExceptionIfNullIsPassedInsteadOfEnvelopeToEgress() {
        assertThatThrownBy(() -> Expectations.expectEgressMessageInExactOrder(null))
                .isInstanceOf(NullExpectedEnvelopeException.class)
                .hasMessage("Expected envelope to egress cannot be null. Use Envelope.builder() to build a message");
    }
}