package com.github.insyde.statefun.tsukuyomi.core.capture;

import com.github.insyde.statefun.tsukuyomi.core.validation.Target;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Type;
import org.apache.flink.statefun.sdk.java.types.TypeSerializer;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Base64;

import static com.github.insyde.statefun.tsukuyomi.core.capture.Envelope.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvelopeTest {

    @Test
    void throwsExceptionIfToIsNull() {
        assertThatThrownBy(() -> builder()
                .data(Types.stringType(), "Foo")
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsExceptionIfDataIsNull() {
        assertThatThrownBy(() -> builder()
                .data(null)
                .toFunction(TypeName.typeNameFromString("foo/bar"), "baz")
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsExceptionIfFromTypenameIsNull() {
        assertThatThrownBy(() -> builder()
                .from(null, "id")
                .data(Types.stringType(), "foo")
                .toFunction(TypeName.typeNameFromString("foo/too"), "id")
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsExceptionIfToTypenameIsNull() {
        assertThatThrownBy(() -> builder()
                .toFunction(null, "id")
                .from(TypeName.typeNameFromString("foo/from"), "id")
                .data(Types.stringType(), "foo")
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void throwsExceptionIfFromIdIsNullOrEmpty(String id) {
        assertThatThrownBy(() -> builder()
                .from(TypeName.typeNameFromString("foo/from"), id)
                .toFunction(TypeName.typeNameFromString("foo/to"), "id")
                .data(Types.stringType(), "foo")
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void throwsExceptionIfToIdIsNullOrEmpty(String id) {
        assertThatThrownBy(() -> builder()
                .toFunction(TypeName.typeNameFromString("foo/to"), id)
                .from(TypeName.typeNameFromString("foo/from"), "id")
                .data(Types.stringType(), "foo")
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void envelopeBuiltViaShortcutsIsTheSameAsEnvelopeBuiltViaPureSetters() {
        Envelope expectedEnvelope = buildEnvelopeViaPureSetters();

        Envelope actualEnvelope = buildEnvelopeViaShortcuts();

        assertThat(actualEnvelope).isEqualTo(expectedEnvelope);
    }

    @Test
    void toStringHasDeserializedValueIfTypeIsKnown() {
        Envelope envelope = builder()
                .toFunction(TypeName.typeNameFromString("foo/to"), "id")
                .data(Types.stringType(), "Foo")
                .build();

        String actual = envelope.getData().toString();

        assertThat(actual).contains("value='Foo'");
    }

    @Test
    void toStringHasBase64ValueIfTypeIsUnknown() {
        resetRenderers();
        byte[] bytes = Types.stringType().typeSerializer().serialize("Foo").toByteArray();
        String base64Value = Base64.getEncoder().encodeToString(bytes);
        Envelope envelope = builder()
                .toFunction(TypeName.typeNameFromString("foo/to"), "id")
                .data(Data.of(Types.stringType().typeName().asTypeNameString(), base64Value))
                .build();

        String actual = envelope.getData().toString();

        assertThat(actual).contains(String.format("value='%s'", base64Value));
    }

    @Test
    void deserializesData() {
        Envelope envelope = builder()
                .toFunction(TypeName.typeNameFromString("foo/to"), "id")
                .data(Types.stringType(), "Foo")
                .build();

        String actual = envelope.extractValue(Types.stringType());

        assertThat(actual).isEqualTo("Foo");
    }

    @Test
    void returnsTrueIfTypeEquals() {
        Envelope envelope = builder()
                .toFunction(TypeName.typeNameFromString("foo/to"), "id")
                .data(Types.stringType(), "Foo")
                .build();

        assertThat(envelope.is(Types.stringType())).isTrue();
    }

    @Test
    void createsTargetFromNode() {
        NodeAddress nodeAddress = NodeAddress.of("foo/bar", "baz", NodeAddress.Type.FUNCTION);

        Target target = nodeAddress.toTarget();

        assertThat(target.getType()).isEqualTo(Target.Type.FUNCTION);
        assertThat(target.getTypeName()).isEqualTo(TypeName.typeNameFromString("foo/bar"));
    }

    private Envelope buildEnvelopeViaPureSetters() {
        Type<String> stringType = Types.stringType();
        TypeSerializer<String> serializer = stringType.typeSerializer();
        return builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .toFunction(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Data.of(
                        stringType.typeName().asTypeNameString(),
                        Base64.getEncoder().encodeToString(serializer.serialize("foobarbaz").toByteArray())
                ))
                .build();
    }

    private Envelope buildEnvelopeViaShortcuts() {
        return builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .toFunction(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}