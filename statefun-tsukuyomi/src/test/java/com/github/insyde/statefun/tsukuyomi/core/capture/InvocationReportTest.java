package com.github.insyde.statefun.tsukuyomi.core.capture;

import com.github.insyde.statefun.tsukuyomi.core.validation.EnvelopeMeta;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationReportTest {

    @Test
    void returnsListOfEnvelopeMetaByGivenEnvelope() {
        Envelope envelope = envelope();
        InvocationReport report = InvocationReport.of(List.of(envelope));

        List<EnvelopeMeta> metas = report.find(envelope);

        assertThat(metas).containsExactly(EnvelopeMeta.of(0));
    }

    @Test
    void returnsNumberOfOutgoingMessages() {
        Envelope envelope = envelope();
        InvocationReport report = InvocationReport.of(List.of(envelope));

        int count = report.countOutgoingMessages();

        assertThat(count).isEqualTo(1);
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .toFunction(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}