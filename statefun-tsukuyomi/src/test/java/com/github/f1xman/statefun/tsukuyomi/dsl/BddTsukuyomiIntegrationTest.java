package com.github.f1xman.statefun.tsukuyomi.dsl;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.validation.GivenFunction;
import com.github.f1xman.statefun.tsukuyomi.testutil.IntegrationTest;
import org.apache.flink.statefun.sdk.java.*;
import org.apache.flink.statefun.sdk.java.message.EgressMessage;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;

import static com.github.f1xman.statefun.tsukuyomi.core.capture.StateValue.empty;
import static com.github.f1xman.statefun.tsukuyomi.core.capture.StateValue.havingValue;
import static com.github.f1xman.statefun.tsukuyomi.dsl.BddTsukuyomi.*;
import static com.github.f1xman.statefun.tsukuyomi.dsl.Criteria.*;
import static org.hamcrest.Matchers.is;

@IntegrationTest
class BddTsukuyomiIntegrationTest {

    static final TypeName COLLABORATOR_1 = TypeName.typeNameFromString("foo/collaborator-1");
    static final TypeName COLLABORATOR_2 = TypeName.typeNameFromString("foo/collaborator-2");
    static final TypeName EGRESS = TypeName.typeNameFromString("foo/egress");
    static final String HELLO = "hello";
    static final String FUNCTION_ID = "functionId";
    static final String BAR = "bar";

    @Test
    @Timeout(30)
    void verifiesThatTheFunctionSendsMessagesInOrderTheyGoInThen() {
        // Define your envelopes
        Envelope envelope = incomingEnvelope();
        Envelope expectedToFunction = outgoingEnvelopeToFunction();
        Envelope expectedToEgress = outgoingEnvelopeToEgress();
        Envelope expectedToSelf = outgoingEnvelopeToSelf().toBuilder().build();
        // Define function under test and its initial state
        GivenFunction testee = given(
                function(Testee.TYPE, new Testee()),
                withState(Testee.FOO, empty()),
                withState(Testee.BAR, havingValue(BAR))
        );

        // When function under test receives that envelope
        when(
                testee,
                receives(envelope)
        ).then(
                // Then expect it sends the following messages
                expectMessageInExactOrder(expectedToFunction),
                expectMessageInExactOrder(expectedToSelf),
                expectEgressMessageInExactOrder(expectedToEgress),
                // and has the following state value after invocation
                expectState(Testee.FOO, is("foo"))
        );
    }

    @Test
    @Timeout(30)
    void verifiesThatTheFunctionSendsMessagesInAnyOrder() {
        Envelope envelope = incomingEnvelope();
        Envelope expectedToFunction = outgoingEnvelopeToFunction();
        Envelope expectedToEgress = outgoingEnvelopeToEgress();
        Envelope expectedToSelf = outgoingEnvelopeToSelf().toBuilder().build();
        GivenFunction testee = given(
                function(Testee.TYPE, new Testee()),
                withState(Testee.FOO, empty()),
                withState(Testee.BAR, havingValue(BAR))
        );

        when(
                testee,
                receives(envelope)
        ).then(
                expectEgressMessageInAnyOrder(expectedToEgress),
                expectMessageInAnyOrder(expectedToSelf),
                expectMessageInAnyOrder(expectedToFunction),
                expectState(Testee.FOO, is("foo"))
        );
    }

    private Envelope outgoingEnvelopeToEgress() {
        return Envelope.builder()
                .toEgress(EGRESS)
                .data(Types.stringType(), HELLO + BAR)
                .build();
    }

    private Envelope outgoingEnvelopeToFunction() {
        return Envelope.builder()
                .from(Testee.TYPE, FUNCTION_ID)
                .to(COLLABORATOR_2, FUNCTION_ID)
                .data(Types.stringType(), HELLO + BAR)
                .build();
    }

    private Envelope outgoingEnvelopeToSelf() {
        return Envelope.builder()
                .from(Testee.TYPE, FUNCTION_ID)
                .to(Testee.TYPE, FUNCTION_ID)
                .data(Types.stringType(), HELLO + BAR)
                .build();
    }

    private Envelope incomingEnvelope() {
        return Envelope.builder()
                .from(COLLABORATOR_1, FUNCTION_ID)
                .to(Testee.TYPE, FUNCTION_ID)
                .data(Types.stringType(), HELLO)
                .build();
    }

    static class Testee implements StatefulFunction {

        static TypeName TYPE = TypeName.typeNameFromString("foo/testee");
        static ValueSpec<String> FOO = ValueSpec.named("foo").withUtf8StringType();
        static ValueSpec<String> BAR = ValueSpec.named("bar").withUtf8StringType();

        @Override
        public CompletableFuture<Void> apply(Context context, Message message) {
            AddressScopedStorage storage = context.storage();
            String bar = storage.get(BAR).orElse("");
            storage.set(FOO, "foo");
            String value = message.asUtf8String() + bar;
            Message toFunction = MessageBuilder.forAddress(COLLABORATOR_2, context.self().id())
                    .withValue(value)
                    .build();
            context.send(toFunction);
            Message toSelf = MessageBuilder.forAddress(context.self())
                    .withValue(value)
                    .build();
            context.send(toSelf);
            EgressMessage toEgress = EgressMessageBuilder.forEgress(EGRESS)
                    .withValue(value)
                    .build();
            context.send(toEgress);
            return context.done();
        }
    }
}