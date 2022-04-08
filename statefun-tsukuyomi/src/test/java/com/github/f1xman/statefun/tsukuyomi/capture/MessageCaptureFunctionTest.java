package com.github.f1xman.statefun.tsukuyomi.capture;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f1xman.statefun.tsukuyomi.SerDe;
import com.github.f1xman.statefun.tsukuyomi.capture.Egresses;
import com.github.f1xman.statefun.tsukuyomi.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.capture.MessageCaptureFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.Address;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.message.EgressMessage;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.apache.flink.statefun.sdk.java.testing.TestContext;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

class MessageCaptureFunctionTest {

    static final Address FROM = new Address(TypeName.typeNameFromString("foo/from"), "bar");
    static final Address TO = new Address(TypeName.typeNameFromString("foo/to"), "bar");

    @Test
    void wrapsIncomingMessageIntoEnvelopeAndSendsToEgress() throws Throwable {
        TestContext context = TestContext.forTargetWithCaller(TO, FROM);
        MessageCaptureFunction testee = MessageCaptureFunction.INSTANCE;
        Greeting greeting = new Greeting("Naruto");
        Message message = MessageBuilder.forAddress(TO)
                .withCustomType(Greeting.TYPE, greeting)
                .build();
        EgressMessage expected = EgressMessageBuilder.forEgress(Egresses.CAPTURED_MESSAGES)
                .withCustomType(Envelope.TYPE, new Envelope(
                        Envelope.NodeAddress.of(FROM.type().asTypeNameString(), FROM.id()),
                        Envelope.NodeAddress.of(TO.type().asTypeNameString(), TO.id()),
                        Envelope.Data.of(
                                Greeting.TYPE.typeName().asTypeNameString(),
                                Base64.getEncoder().encodeToString(SerDe.serialize(greeting))
                        )
                ))
                .build();

        testee.apply(context, message);

        assertThat(context.getSentEgressMessages()).hasSize(1);
        assertThat(context.getSentEgressMessages().get(0).message()).isEqualTo(expected);
    }

    @RequiredArgsConstructor(onConstructor_ = @JsonCreator)
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    @Getter
    private static class Greeting {

        private static final Type<Greeting> TYPE = SimpleType.simpleImmutableTypeFrom(
                TypeName.typeNameFromString("foo/greeting"),
                SerDe::serialize, bytes -> SerDe.deserialize(bytes, Greeting.class)
        );

        @JsonProperty("name")
        String name;

    }
}