package com.github.f1xman.statefun.tsukuyomi.core.capture;

import com.github.f1xman.statefun.tsukuyomi.core.validation.EnvelopeMeta;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationReportTest {

    @Test
    void returnsTrueIfEnvelopeHasGivenIndex() {
        Envelope envelope = envelope();
        InvocationReport report = InvocationReport.of(0, List.of(envelope));

        assertThat(report.containsAt(envelope, 0)).isTrue();
    }

    @Test
    void returnsFalseIfEnvelopeHasDifferentIndex() {
        Envelope envelope = envelope();
        InvocationReport report = InvocationReport.of(0, List.of(envelope));

        assertThat(report.containsAt(envelope, 1)).isFalse();
    }

    @Test
    void returnsFalseIfEnvelopeDoesNotExist() {
        Envelope envelope = envelope();
        InvocationReport report = InvocationReport.of(0, List.of());

        assertThat(report.containsAt(envelope, 0)).isFalse();
    }

    @Test
    void returnsListOfEnvelopeMetaByGivenEnvelope() {
        Envelope envelope = envelope();
        InvocationReport report = InvocationReport.of(0, List.of(envelope));

        List<EnvelopeMeta> metas = report.find(envelope);

        assertThat(metas).containsExactly(EnvelopeMeta.of(0));
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .to(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}