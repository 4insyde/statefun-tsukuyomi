package com.github.insyde.statefun.tsukuyomi.core.validation;

import com.github.insyde.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReceivedMessageExplorerImplTest {

    @Test
    void findsContainingEnvelope() {
        Envelope envelope = envelope();
        ReceivedMessageExplorer receivedMessageExplorer = ReceivedMessageExplorerImpl.of(List.of(envelope));

        assertThat(receivedMessageExplorer.findAndHide(envelope)).isTrue();
    }

    @Test
    void returnsFalseIfGivenEnvelopeDoesNotExist() {
        Envelope envelope = envelope();
        ReceivedMessageExplorer receivedMessageExplorer = ReceivedMessageExplorerImpl.of(List.of());

        assertThat(receivedMessageExplorer.findAndHide(envelope)).isFalse();
    }

    @Test
    void doesNotFindTheSameEnvelopeTwice() {
        Envelope envelope = envelope();
        ReceivedMessageExplorer receivedMessageExplorer = ReceivedMessageExplorerImpl.of(List.of(envelope));

        receivedMessageExplorer.findAndHide(envelope);

        assertThat(receivedMessageExplorer.findAndHide(envelope)).isFalse();
    }

    @Test
    void findsEqualEnvelopesOneTimeEach() {
        Envelope envelope = envelope();
        ReceivedMessageExplorer receivedMessageExplorer = ReceivedMessageExplorerImpl.of(List.of(envelope, envelope));

        receivedMessageExplorer.findAndHide(envelope);

        assertThat(receivedMessageExplorer.findAndHide(envelope)).isTrue();
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .toFunction(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}