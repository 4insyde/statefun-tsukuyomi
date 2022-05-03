package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.InvocationReport;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.when;

@ExtendWith(MockitoExtension.class)
class ExpectMessageInExactOrderTest {

    @Mock
    TsukuyomiApi tsukuyomi;

    @Test
    void throwsExceptionIfEnvelopeDoesNotMatch() {
        Envelope envelope = envelope();
        Envelope notMatchingEnvelope = copyFromOfTo(envelope);
        ExpectMessageInExactOrder expectMessage = ExpectMessageInExactOrder.of(notMatchingEnvelope, Target.Type.EGRESS);
        when(tsukuyomi.getReceived()).thenReturn(List.of(envelope));

        assertThatThrownBy(
                () -> expectMessage.match(0, tsukuyomi, Set.of()))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void throwsExceptionIfMatchingEnvelopeIsNotRegular() {
        Envelope envelope = envelope();
        ExpectMessageInExactOrder expectMessage = ExpectMessageInExactOrder.of(envelope, Target.Type.FUNCTION);
        when(tsukuyomi.getReceived()).thenReturn(List.of(envelope));
        when(tsukuyomi.getInvocationReport()).thenReturn(Optional.of(InvocationReport.of(0, List.of())));

        assertThatThrownBy(
                () -> expectMessage.match(0, tsukuyomi, Set.of()))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void returnsIndexOfMatchedEnvelope() {
        Envelope envelope = envelope();
        ExpectMessageInExactOrder expectMessage = ExpectMessageInExactOrder.of(envelope, Target.Type.EGRESS);
        when(tsukuyomi.getReceived()).thenReturn(List.of(envelope));

        Integer actualIndex = expectMessage.match(0, tsukuyomi, Set.of());

        assertThat(actualIndex).isZero();
    }

    @Test
    void matchesEnvelopesByOrderScopedToTarget() {
        Envelope targetAEnvelopeA = envelope().toBuilder()
                .to(TypeName.typeNameFromString("foo/a"), "foobaz")
                .data(Types.stringType(), "a")
                .build();
        Envelope targetAEnvelopeB = envelope().toBuilder()
                .to(TypeName.typeNameFromString("foo/a"), "foobaz")
                .data(Types.stringType(), "b")
                .build();
        Envelope targetBEnvelopeB = envelope().toBuilder()
                .to(TypeName.typeNameFromString("foo/b"), "foobaz")
                .data(Types.stringType(), "b")
                .build();
        ExpectMessageInExactOrder expectMessage = ExpectMessageInExactOrder.of(targetAEnvelopeA, Target.Type.EGRESS);
        when(tsukuyomi.getReceived()).thenReturn(List.of(targetBEnvelopeB, targetAEnvelopeA, targetAEnvelopeB));

        expectMessage.match(0, tsukuyomi, Set.of());
    }

    @Test
    void targetIsFunctionThatHasTheSameTypeNameAsEnvelopeTo() {
        Envelope envelope = envelope();
        ExpectMessageInExactOrder expectMessage = ExpectMessageInExactOrder.of(envelope, Target.Type.FUNCTION);

        Target actual = expectMessage.getTarget();

        assertThat(actual.getType()).isEqualTo(Target.Type.FUNCTION);
        assertThat(actual.getTypeName().asTypeNameString()).isEqualTo(envelope.getTo().getType());
    }

    @Test
    void throwsAssertionErrorIfOrderIsBiggerThanNumberOfReceivedEnvelopes() {
        Envelope envelope = envelope();
        ExpectMessageInExactOrder expectMessage = ExpectMessageInExactOrder.of(envelope, Target.Type.FUNCTION);
        when(tsukuyomi.getReceived()).thenReturn(List.of(envelope));

        assertThatThrownBy(
                () -> expectMessage.match(1, tsukuyomi, Set.of()))
                .isInstanceOf(AssertionError.class);
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .to(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }

    private Envelope copyFromOfTo(Envelope originalEnvelope) {
        return originalEnvelope.toBuilder()
                .from(originalEnvelope.getTo())
                .build();
    }
}