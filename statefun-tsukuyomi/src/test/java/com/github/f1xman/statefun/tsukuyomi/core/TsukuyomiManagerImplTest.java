package com.github.f1xman.statefun.tsukuyomi.core;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.ModuleDefinition;
import com.github.f1xman.statefun.tsukuyomi.core.validation.TsukuyomiApi;
import com.github.f1xman.statefun.tsukuyomi.core.validation.TsukuyomiManagerImpl;
import com.github.f1xman.statefun.tsukuyomi.testutil.IntegrationTest;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.message.EgressMessage;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@IntegrationTest
class TsukuyomiManagerImplTest {

    static final TypeName COLLABORATOR = TypeName.typeNameFromString("foo/collaborator");
    static final TypeName EGRESS = TypeName.typeNameFromString("foo/egress");
    static final String HELLO = "hello";
    static final String FUNCTION_ID = "bar";

    @Test
    @Timeout(60)
    void startsTsukuyomiAndExchangesMessages() {
        Assertions.assertTimeoutPreemptively(Duration.ofMinutes(1), () -> {
            TsukuyomiManagerImpl tsukuyomiManager = new TsukuyomiManagerImpl();
            Testee testee = new Testee();
            ModuleDefinition.FunctionDefinition functionDefinition = ModuleDefinition.FunctionDefinition.builder()
                    .typeName(Testee.TYPE)
                    .instance(testee)
                    .stateSetters(List.of())
                    .build();
            ModuleDefinition moduleDefinition = ModuleDefinition.builder()
                    .functionUnderTest(functionDefinition)
                    .collaborator(COLLABORATOR)
                    .egress(EGRESS)
                    .build();
            TsukuyomiApi client = tsukuyomiManager.start(moduleDefinition);
            Envelope envelope = Envelope.builder()
                    .from(COLLABORATOR, FUNCTION_ID)
                    .to(Testee.TYPE, FUNCTION_ID)
                    .data(Types.stringType(), HELLO)
                    .build();
            Envelope expectedToFunction = envelope.toBuilder()
                    .from(Testee.TYPE, FUNCTION_ID)
                    .to(COLLABORATOR, FUNCTION_ID)
                    .build();
            Envelope expectedToEgress = envelope.toBuilder()
                    .from(null)
                    .to(EGRESS, null)
                    .build();

            client.send(envelope);
            Collection<Envelope> received = client.getReceived();
            await().atMost(Duration.ofMinutes(1)).until(() -> received.size() >= 2);
            assertThat(received).contains(expectedToFunction, expectedToEgress);

            tsukuyomiManager.stop();
        });
    }

    static class Testee implements StatefulFunction {

        static TypeName TYPE = TypeName.typeNameFromString("foo/testee");

        @Override
        public CompletableFuture<Void> apply(Context context, Message message) {
            Message toFunction = MessageBuilder.fromMessage(message)
                    .withTargetAddress(COLLABORATOR, context.self().id())
                    .build();
            context.send(toFunction);
            EgressMessage toEgress = EgressMessageBuilder.forEgress(EGRESS)
                    .withValue(message.asUtf8String())
                    .build();
            context.send(toEgress);
            return context.done();
        }
    }
}