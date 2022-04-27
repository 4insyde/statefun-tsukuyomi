package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpectMessageInAnyOrderTest {

    @Mock
    TsukuyomiApi tsukuyomi;

    @Test
    void throwsExceptionIfEnvelopeDoesNotMatch() {
        Envelope envelope = envelope();
        Envelope notMatchingEnvelope = copyFromOfTo(envelope);
        ExpectMessageInAnyOrder expectMessage = ExpectMessageInAnyOrder.of(notMatchingEnvelope, Target.Type.FUNCTION, new HashSet<>());
        when(tsukuyomi.getReceived()).thenReturn(List.of(envelope));

        assertThatThrownBy(
                () -> expectMessage.match(0, tsukuyomi))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void nothingThrownWhenEnvelopeMatches() {
        Envelope envelope = envelope();
        ExpectMessageInAnyOrder expectMessage = ExpectMessageInAnyOrder.of(envelope, Target.Type.FUNCTION, new HashSet<>());
        when(tsukuyomi.getReceived()).thenReturn(List.of(envelope));

        expectMessage.match(0, tsukuyomi);
    }

    @Test
    void targetIsFunctionThatHasTheSameTypeNameAsEnvelopeTo() {
        Envelope envelope = envelope();
        ExpectMessageInAnyOrder expectMessage = ExpectMessageInAnyOrder.of(envelope, Target.Type.FUNCTION, new HashSet<>());

        Target actual = expectMessage.getTarget();

        assertThat(actual.getType()).isEqualTo(Target.Type.FUNCTION);
        assertThat(actual.getTypeName().asTypeNameString()).isEqualTo(envelope.getTo().getType());
    }

    @Test
    void doesNotMatchTheSameEnvelopeTwice() {
        Envelope envelope = envelope();
        HashSet<Integer> exhaustedIndexes = new HashSet<>();
        ExpectMessageInAnyOrder expectMessage1 = ExpectMessageInAnyOrder.of(envelope, Target.Type.FUNCTION, exhaustedIndexes);
        ExpectMessageInAnyOrder expectMessage2 = ExpectMessageInAnyOrder.of(envelope, Target.Type.FUNCTION, exhaustedIndexes);
        when(tsukuyomi.getReceived()).thenReturn(List.of(envelope));

        expectMessage1.match(0, tsukuyomi);

        assertThatThrownBy(() -> expectMessage2.match(0, tsukuyomi))
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