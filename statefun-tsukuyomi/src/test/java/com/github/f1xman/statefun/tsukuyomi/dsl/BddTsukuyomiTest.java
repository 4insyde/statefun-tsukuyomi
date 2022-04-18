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
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.is;

@IntegrationTest
class BddTsukuyomiTest {

    static final TypeName COLLABORATOR_1 = TypeName.typeNameFromString("foo/collaborator-1");
    static final TypeName COLLABORATOR_2 = TypeName.typeNameFromString("foo/collaborator-2");
    static final TypeName EGRESS = TypeName.typeNameFromString("foo/egress");
    static final String HELLO = "hello";
    static final String FUNCTION_ID = "functionId";
    static final String BAR = "bar";

    @Test
    @Timeout(value = 1, unit = MINUTES)
    void exchangesMessages() {
        Envelope envelope = incomingEnvelope();
        Envelope expectedToFunction = outgoingEnvelope();
        Envelope expectedToEgress = expectedToFunction.toBuilder()
                .from(null)
                .to(EGRESS, null)
                .build();
        GivenFunction testee = given(
                function(Testee.TYPE, new Testee()),
                withState(Testee.FOO, empty()),
                withState(Testee.BAR, havingValue(BAR))
        );

        when(
                testee,
                receives(envelope)
        ).then(
                Expectations.expectMessageToFunction(expectedToFunction),
                Expectations.expectMessageToEgress(expectedToEgress),
                Expectations.expectState(Testee.FOO, is("foo"))
        );
    }

    private Envelope outgoingEnvelope() {
        return Envelope.builder()
                .from(Testee.TYPE, FUNCTION_ID)
                .to(COLLABORATOR_2, FUNCTION_ID)
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
            EgressMessage toEgress = EgressMessageBuilder.forEgress(EGRESS)
                    .withValue(value)
                    .build();
            context.send(toEgress);
            return context.done();
        }
    }
}