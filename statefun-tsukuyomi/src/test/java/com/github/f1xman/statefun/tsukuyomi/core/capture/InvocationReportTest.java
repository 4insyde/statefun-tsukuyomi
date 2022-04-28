package com.github.f1xman.statefun.tsukuyomi.core.capture;

import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationReportTest {

    @Test
    void isRegularReturnsTrueIfEnvelopeWasSentImmediately() {
        Envelope envelope = envelope();
        InvocationReport report = InvocationReport.of(0, List.of(envelope));

        boolean actual = report.isRegular(envelope);

        assertThat(actual).isTrue();
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .to(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}