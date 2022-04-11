package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.capture.Envelope;
import org.apache.flink.statefun.sdk.java.*;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import static com.github.f1xman.statefun.tsukuyomi.api.StateValue.empty;
import static com.github.f1xman.statefun.tsukuyomi.api.Tsukuyomi.*;
import static org.hamcrest.Matchers.is;

class TsukuyomiTest {

    static final TypeName COLLABORATOR = TypeName.typeNameFromString("foo/collaborator");
    static final String HELLO = "hello";
    static final String FUNCTION_ID = "bar";

    @Test
    void exchangesMessages() {
        Envelope envelope = incomingEnvelope();
        Envelope expected = outgoingEnvelope();
        GivenFunction testee = given(
                function(Testee.TYPE, new Testee()),
                withState(Testee.VALUE, empty())
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
                .from(Envelope.NodeAddress.of(Testee.TYPE.asTypeNameString(), FUNCTION_ID))
                .to(Envelope.NodeAddress.of(COLLABORATOR.asTypeNameString(), FUNCTION_ID))
                .data(Envelope.Data.of(
                        Types.stringType().typeName().asTypeNameString(),
                        Base64.getEncoder().encodeToString(Types.stringType().typeSerializer().serialize(HELLO).toByteArray())
                ))
                .build();
    }

    private Envelope incomingEnvelope() {
        return Envelope.builder()
                .from(Envelope.NodeAddress.of(COLLABORATOR.asTypeNameString(), FUNCTION_ID))
                .to(Envelope.NodeAddress.of(Testee.TYPE.asTypeNameString(), FUNCTION_ID))
                .data(Envelope.Data.of(
                        Types.stringType().typeName().asTypeNameString(),
                        Base64.getEncoder().encodeToString(Types.stringType().typeSerializer().serialize(HELLO).toByteArray())
                ))
                .build();
    }

    static class Testee implements StatefulFunction {

        static TypeName TYPE = TypeName.typeNameFromString("foo/testee");
        static ValueSpec<String> VALUE = ValueSpec.named("value").withUtf8StringType();

        @Override
        public CompletableFuture<Void> apply(Context context, Message message) {
            AddressScopedStorage storage = context.storage();
            storage.set(VALUE, "foo");
            Message outgoingMessage = MessageBuilder.fromMessage(message)
                    .withTargetAddress(COLLABORATOR, context.self().id())
                    .build();
            context.send(outgoingMessage);
            return context.done();
        }
    }
}