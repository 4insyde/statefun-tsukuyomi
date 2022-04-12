package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.testutil.IntegrationTest;
import org.apache.flink.statefun.sdk.java.*;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;

import static com.github.f1xman.statefun.tsukuyomi.api.StateValue.empty;
import static com.github.f1xman.statefun.tsukuyomi.api.StateValue.havingValue;
import static com.github.f1xman.statefun.tsukuyomi.api.Tsukuyomi.*;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.is;

@IntegrationTest
class TsukuyomiTest {

    static final TypeName COLLABORATOR = TypeName.typeNameFromString("foo/collaborator");
    static final String HELLO = "hello";
    static final String FUNCTION_ID = "functionId";
    static final String BAR = "bar";

    @Test
    @Timeout(value = 1, unit = MINUTES)
    void exchangesMessages() {
        Envelope envelope = incomingEnvelope();
        Envelope expected = outgoingEnvelope();
        GivenFunction testee = given(
                function(Testee.TYPE, new Testee()),
                withState(Testee.FOO, empty()),
                withState(Testee.BAR, havingValue(BAR))
        );

        when(
                testee,
                receives(envelope)
        ).then(
                expectMessage(is(expected))
        );
    }

    private Envelope outgoingEnvelope() {
        return Envelope.builder()
                .from(Testee.TYPE, FUNCTION_ID)
                .to(COLLABORATOR, FUNCTION_ID)
                .data(Types.stringType(), HELLO + BAR)
                .build();
    }

    private Envelope incomingEnvelope() {
        return Envelope.builder()
                .from(COLLABORATOR, FUNCTION_ID)
                .to(Testee.TYPE, FUNCTION_ID)
                .data(Types.stringType(), HELLO)
                .build();
    }

    static class Testee implements StatefulFunction {

        static TypeName TYPE = TypeName.typeNameFromString("foo/testee");
        static ValueSpec<String> FOO = ValueSpec.named("foo").withUtf8StringType();
        static ValueSpec<String> BAR = ValueSpec.named(TsukuyomiTest.BAR).withUtf8StringType();

        @Override
        public CompletableFuture<Void> apply(Context context, Message message) {
            AddressScopedStorage storage = context.storage();
            String incomingData = message.asUtf8String();
            String bar = storage.get(BAR).orElse("");
            Message outgoingMessage = MessageBuilder.forAddress(COLLABORATOR, context.self().id())
                    .withValue(incomingData + bar)
                    .build();
            context.send(outgoingMessage);
            return context.done();
        }
    }
}