package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.when;

@ExtendWith(MockitoExtension.class)
class ExpectMessageTest {

    @Mock
    TsukuyomiApi tsukuyomi;
    @Mock
    DefinitionOfReady mockedDefinitionOfReady;

    @Test
    void throwsExceptionIfEnvelopeDoesNotMatch() {
        Envelope envelope = envelope();
        Envelope notMatchingEnvelope = copyFromOfTo(envelope);
        ExpectMessage expectMessage = ExpectMessage.of(notMatchingEnvelope, Target.Type.FUNCTION);
        when(tsukuyomi.getReceived()).thenReturn(List.of(envelope));

        assertThatThrownBy(
                () -> expectMessage.match(0, tsukuyomi))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void nothingThrownWhenEnvelopeMatches() {
        Envelope envelope = envelope();
        ExpectMessage expectMessage = ExpectMessage.of(envelope, Target.Type.FUNCTION);
        when(tsukuyomi.getReceived()).thenReturn(List.of(envelope));

        expectMessage.match(0, tsukuyomi);
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
        ExpectMessage expectMessage = ExpectMessage.of(targetAEnvelopeA, Target.Type.FUNCTION);
        when(tsukuyomi.getReceived()).thenReturn(List.of(targetBEnvelopeB, targetAEnvelopeA, targetAEnvelopeB));

        expectMessage.match(0, tsukuyomi);
    }

    @Test
    void targetIsFunctionThatHasTheSameTypeNameAsEnvelopeTo() {
        Envelope envelope = envelope();
        ExpectMessage expectMessage = ExpectMessage.of(envelope, Target.Type.FUNCTION);

        Optional<Target> actual = expectMessage.getTarget();

        assertThat(actual).hasValueSatisfying(t -> {
            assertThat(t.getType()).isEqualTo(Target.Type.FUNCTION);
            assertThat(t.getTypeName().asTypeNameString()).isEqualTo(envelope.getTo().getType());
        });
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