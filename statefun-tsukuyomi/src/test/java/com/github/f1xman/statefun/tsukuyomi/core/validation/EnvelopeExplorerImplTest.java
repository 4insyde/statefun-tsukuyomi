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

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EnvelopeExplorerImplTest {

    @Mock
    TsukuyomiApi tsukuyomi;

    @Test
    void returnsEnvelopeSummary() {
        Envelope envelope = envelope();
        Envelope envelope1 = envelope1();
        InvocationReport invocationReport = InvocationReport.of(List.of(envelope, envelope1));
        given(tsukuyomi.getInvocationReport()).willReturn(Optional.of(invocationReport));
        List<Envelope> receivedEnvelopes = List.of(envelope, envelope1);
        given(tsukuyomi.getReceived()).willReturn(receivedEnvelopes);
        EnvelopeExplorer explorer = EnvelopeExplorerImpl.of(tsukuyomi);

        EnvelopeSummary summary = explorer.explore(envelope);

        assertThat(summary.getEnvelopeMetas()).containsExactly(EnvelopeMeta.of(0));
        assertThat(summary.getTotalReceived()).isEqualTo(1);
    }

    @Test
    void includesEqualEnvelopeToSummaryEvenThoughDelayIsMissing() {
        Envelope envelopeWithDelay = envelopeWithDelay();
        Envelope envelopeWithoutDelay = envelope();
        InvocationReport invocationReport = InvocationReport.of(List.of(envelopeWithDelay));
        given(tsukuyomi.getInvocationReport()).willReturn(Optional.of(invocationReport));
        List<Envelope> receivedEnvelopes = List.of(envelopeWithoutDelay);
        given(tsukuyomi.getReceived()).willReturn(receivedEnvelopes);
        EnvelopeExplorer explorer = EnvelopeExplorerImpl.of(tsukuyomi);

        EnvelopeSummary summary = explorer.explore(envelopeWithDelay);

        assertThat(summary.getEnvelopeMetas()).containsExactly(EnvelopeMeta.of(0));
        assertThat(summary.getTotalReceived()).isEqualTo(1);
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .toFunction(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }

    private Envelope envelopeWithDelay() {
        return envelope().toBuilder()
                .delay(Duration.ofSeconds(1))
                .build();
    }

    private Envelope envelope1() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "1")
                .toFunction(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}