package com.github.f1xman.statefun.tsukuyomi.core.capture;

import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Type;
import org.apache.flink.statefun.sdk.java.types.TypeSerializer;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeTest {

    @Test
    void envelopeBuiltViaShortcutsIsTheSameAsEnvelopeBuiltViaPureSetters() {
        Envelope expectedEnvelope = buildEnvelopeViaPureSetters();

        Envelope actualEnvelope = buildEnvelopeViaShortcuts();

        assertThat(actualEnvelope).isEqualTo(expectedEnvelope);
    }

    @Test
    void toStringHaveDeserializedValueIfTypeIsKnown() {
        Envelope envelope = Envelope.builder()
                .data(Types.stringType(), "Foo")
                .build();

        String actual = envelope.getData().toString();

        assertThat(actual).contains("value='Foo'");
    }

    @Test
    void toStringHaveBase64ValueIfTypeIsUnknown() {
        Envelope.resetRenderers();
        byte[] bytes = Types.stringType().typeSerializer().serialize("Foo").toByteArray();
        String base64Value = Base64.getEncoder().encodeToString(bytes);
        Envelope envelope = Envelope.builder()
                .data(Envelope.Data.of(Types.stringType().typeName().asTypeNameString(), base64Value))
                .build();

        String actual = envelope.getData().toString();

        assertThat(actual).contains(String.format("value='%s'", base64Value));
    }

    private Envelope buildEnvelopeViaPureSetters() {
        Type<String> stringType = Types.stringType();
        TypeSerializer<String> serializer = stringType.typeSerializer();
        return Envelope.builder()
                .from(Envelope.NodeAddress.of("foo/bar", "foobar"))
                .to(Envelope.NodeAddress.of("foo/baz", "foobaz"))
                .data(Envelope.Data.of(
                        stringType.typeName().asTypeNameString(),
                        Base64.getEncoder().encodeToString(serializer.serialize("foobarbaz").toByteArray())
                ))
                .build();
    }

    private Envelope buildEnvelopeViaShortcuts() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .to(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}