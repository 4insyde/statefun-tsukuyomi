package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

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
        ExpectMessageInAnyOrder expectMessage = ExpectMessageInAnyOrder.of(notMatchingEnvelope, Target.Type.FUNCTION);
        when(tsukuyomi.getReceived()).thenReturn(List.of(envelope));

        assertThatThrownBy(
                () -> expectMessage.match(0, tsukuyomi, Set.of()))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void returnsIndexOfMatchedEnvelope() {
        Envelope envelope = envelope();
        ExpectMessageInAnyOrder expectMessage = ExpectMessageInAnyOrder.of(envelope, Target.Type.FUNCTION);
        when(tsukuyomi.getReceived()).thenReturn(List.of(envelope));

        Integer actualIndex = expectMessage.match(0, tsukuyomi, Set.of());

        assertThat(actualIndex).isZero();
    }

    @Test
    void targetIsFunctionThatHasTheSameTypeNameAsEnvelopeTo() {
        Envelope envelope = envelope();
        ExpectMessageInAnyOrder expectMessage = ExpectMessageInAnyOrder.of(envelope, Target.Type.FUNCTION);

        Target actual = expectMessage.getTarget();

        assertThat(actual.getType()).isEqualTo(Target.Type.FUNCTION);
        assertThat(actual.getTypeName().asTypeNameString()).isEqualTo(envelope.getTo().getType());
    }

    @Test
    void doesNotMatchTheEnvelopeWithBlacklistedIndex() {
        Envelope envelope = envelope();
        Set<Integer> indexBlacklist = Set.of(0);
        ExpectMessageInAnyOrder expectMessage = ExpectMessageInAnyOrder.of(envelope, Target.Type.FUNCTION);
        when(tsukuyomi.getReceived()).thenReturn(List.of(envelope));

        assertThatThrownBy(() -> expectMessage.match(0, tsukuyomi, indexBlacklist))
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