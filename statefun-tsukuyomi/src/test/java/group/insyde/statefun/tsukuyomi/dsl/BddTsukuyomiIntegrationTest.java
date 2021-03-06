package group.insyde.statefun.tsukuyomi.dsl;

import group.insyde.statefun.tsukuyomi.core.capture.Envelope;
import group.insyde.statefun.tsukuyomi.core.validation.GivenFunction;
import group.insyde.statefun.tsukuyomi.core.capture.StateValue;
import org.apache.flink.statefun.sdk.java.*;
import org.apache.flink.statefun.sdk.java.message.EgressMessage;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static group.insyde.statefun.tsukuyomi.dsl.BddTsukuyomi.*;
import static group.insyde.statefun.tsukuyomi.dsl.Criteria.*;
import static org.hamcrest.Matchers.is;

class BddTsukuyomiIntegrationTest {

    static final TypeName COLLABORATOR_1 = TypeName.typeNameFromString("foo/collaborator-1");
    static final TypeName COLLABORATOR_2 = TypeName.typeNameFromString("foo/collaborator-2");
    static final TypeName EGRESS = TypeName.typeNameFromString("foo/egress");
    static final String HELLO = "hello";
    static final String FUNCTION_ID = "functionId";
    static final String BAR = "bar";
    public static final Duration DELAY = Duration.ofSeconds(1);

    @Test
    @Timeout(30)
    void verifiesThatTheFunctionSendsMessagesInOrderTheyExpected() {
        // Define your envelopes
        Envelope envelope = incomingEnvelope();
        Envelope expectedToFunction = outgoingEnvelopeToFunction();
        Envelope expectedToEgress = outgoingEnvelopeToEgress();
        Envelope expectedToSelf = outgoingEnvelopeToSelf();
        Envelope expectedToFunctionDelayed = delayedEnvelopeToFunction();
        // Define function under test and its initial state
        GivenFunction testee = given(
                function(Testee.TYPE, new Testee()),
                withState(Testee.FOO, StateValue.empty()),
                withState(Testee.BAR, StateValue.havingValue(BAR))
        );

        // When function under test receives that envelope
        when(
                testee,
                receives(envelope)
        ).then(
                // Then expect it sends the following messages
                sendsInOrder(expectedToFunction),
                sendsInOrder(expectedToSelf),
                sendsInOrder(expectedToEgress),
                sendsInOrder(expectedToFunctionDelayed),
                // and has the following state value after invocation
                state(Testee.FOO, is("foo"))
        );
    }

    @Test
    @Timeout(30)
    void verifiesThatTheFunctionSendsMessagesInAnyOrder() {
        Envelope envelope = incomingEnvelope();
        Envelope expectedToFunction = outgoingEnvelopeToFunction();
        Envelope expectedToEgress = outgoingEnvelopeToEgress();
        Envelope expectedToSelf = outgoingEnvelopeToSelf();
        Envelope expectedToFunctionDelayed = delayedEnvelopeToFunction();
        GivenFunction testee = given(
                function(Testee.TYPE, new Testee()),
                withState(Testee.FOO, StateValue.empty()),
                withState(Testee.BAR, StateValue.havingValue(BAR))
        );

        when(
                testee,
                receives(envelope)
        ).then(
                sends(expectedToEgress),
                sends(expectedToSelf),
                sends(expectedToFunction),
                sends(expectedToFunctionDelayed),
                state(Testee.FOO, is("foo"))
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
                .toFunction(COLLABORATOR_2, FUNCTION_ID)
                .data(Types.stringType(), HELLO + BAR)
                .build();
    }

    private Envelope delayedEnvelopeToFunction() {
        return Envelope.builder()
                .from(Testee.TYPE, FUNCTION_ID)
                .toFunction(COLLABORATOR_2, FUNCTION_ID)
                .data(Types.stringType(), HELLO + BAR)
                .delay(DELAY)
                .build();
    }

    private Envelope outgoingEnvelopeToSelf() {
        return Envelope.builder()
                .from(Testee.TYPE, FUNCTION_ID)
                .toFunction(Testee.TYPE, FUNCTION_ID)
                .data(Types.stringType(), HELLO + BAR)
                .build();
    }

    private Envelope incomingEnvelope() {
        return Envelope.builder()
                .from(COLLABORATOR_1, FUNCTION_ID)
                .toFunction(Testee.TYPE, FUNCTION_ID)
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
            context.sendAfter(Duration.ofSeconds(1), toFunction);
            return context.done();
        }
    }
}