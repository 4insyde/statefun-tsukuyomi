package com.github.f1xman.statefun.tsukuyomi.core.capture;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f1xman.statefun.tsukuyomi.util.SerDe;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MessageCaptureFunctionTest {

    static final Address FROM = new Address(TypeName.typeNameFromString("foo/from"), "bar");
    static final Address TO = new Address(TypeName.typeNameFromString("foo/to"), "bar");

    @Test
    void wrapsIncomingMessageIntoEnvelopeAndSendsToEgress() throws Throwable {
        TestContext context = TestContext.forTargetWithCaller(TO, FROM);
        MessageCaptureFunction testee = MessageCaptureFunction.INSTANCE;
        Greeting greeting = new Greeting("Naruto");
        long timestamp = System.nanoTime();
        Message message = MessageBuilder.forAddress(TO)
                .withCustomType(Greeting.TYPE, greeting)
                .build();
        Envelope envelope = Envelope.builder()
                .from(FROM.type(), FROM.id())
                .toFunction(TO.type(), TO.id())
                .data(Greeting.TYPE, greeting)
                .build();
        EgressMessage expected = EgressMessageBuilder.forEgress(Egresses.CAPTURED_MESSAGES)
                .withCustomType(Envelope.TYPE, envelope)
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