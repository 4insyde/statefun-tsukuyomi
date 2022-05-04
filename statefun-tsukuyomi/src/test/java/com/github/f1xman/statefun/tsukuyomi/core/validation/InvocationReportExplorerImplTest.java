package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.InvocationReport;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationReportExplorerImplTest {

    @Test
    void findsContainingEnvelope() {
        Envelope envelope = envelope();
        InvocationReport report = InvocationReport.of(1, List.of(envelope));
        InvocationReportExplorer invocationReportExplorer = InvocationReportExplorerImpl.of(report);

        assertThat(invocationReportExplorer.findAndHide(envelope)).isTrue();
    }

    @Test
    void returnsFalseIfGivenEnvelopeDoesNotExist() {
        Envelope envelope = envelope();
        InvocationReport report = InvocationReport.of(1, List.of());
        InvocationReportExplorer invocationReportExplorer = InvocationReportExplorerImpl.of(report);

        assertThat(invocationReportExplorer.findAndHide(envelope)).isFalse();
    }

    @Test
    void doesNotFindTheSameEnvelopeTwice() {
        Envelope envelope = envelope();
        InvocationReport report = InvocationReport.of(1, List.of(envelope));
        InvocationReportExplorer invocationReportExplorer = InvocationReportExplorerImpl.of(report);

        invocationReportExplorer.findAndHide(envelope);

        assertThat(invocationReportExplorer.findAndHide(envelope)).isFalse();
    }

    @Test
    void findsEqualEnvelopesOneTimeEach() {
        Envelope envelope = envelope();
        InvocationReport report = InvocationReport.of(2, List.of(envelope, envelope));
        InvocationReportExplorer invocationReportExplorer = InvocationReportExplorerImpl.of(report);

        invocationReportExplorer.findAndHide(envelope);

        assertThat(invocationReportExplorer.findAndHide(envelope)).isTrue();
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .to(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}