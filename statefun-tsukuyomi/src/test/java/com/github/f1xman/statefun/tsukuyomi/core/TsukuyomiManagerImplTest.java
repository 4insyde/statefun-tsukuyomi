package com.github.f1xman.statefun.tsukuyomi.core;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class TsukuyomiManagerImplTest {

    static final TypeName COLLABORATOR = TypeName.typeNameFromString("foo/collaborator");
    static final String HELLO = "hello";
    static final String FUNCTION_ID = "bar";

    @Test
    void startsTsukuyomiAndExchangesMessages() {
        Assertions.assertTimeoutPreemptively(Duration.ofMinutes(1), () -> {
            TsukuyomiManagerImpl tsukuyomiManager = new TsukuyomiManagerImpl();
            Testee testee = new Testee();
            ModuleDefinition moduleDefinition = ModuleDefinition.builder()
                    .functionUnderTest(ModuleDefinition.FunctionDefinition.of(Testee.TYPE, testee, List.of()))
                    .collaborator(COLLABORATOR)
                    .build();
            TsukuyomiApi client = tsukuyomiManager.start(moduleDefinition);
            Envelope envelope = Envelope.builder()
                    .from(Envelope.NodeAddress.of(COLLABORATOR.asTypeNameString(), FUNCTION_ID))
                    .to(Envelope.NodeAddress.of(Testee.TYPE.asTypeNameString(), FUNCTION_ID))
                    .data(Envelope.Data.of(
                            Types.stringType().typeName().asTypeNameString(),
                            Base64.getEncoder().encodeToString(Types.stringType().typeSerializer().serialize(HELLO).toByteArray())
                    ))
                    .build();
            Envelope expectedEnvelope = envelope.toBuilder()
                    .from(Envelope.NodeAddress.of(Testee.TYPE.asTypeNameString(), FUNCTION_ID))
                    .to(Envelope.NodeAddress.of(COLLABORATOR.asTypeNameString(), FUNCTION_ID))
                    .build();

            client.send(expectedEnvelope);
            Collection<Envelope> received = client.getReceived();
            await().atMost(Duration.ofMinutes(1)).until(() -> !received.isEmpty());
            assertThat(received).containsOnly(expectedEnvelope);

            tsukuyomiManager.stop();
        });
    }

    static class Testee implements StatefulFunction {

        static TypeName TYPE = TypeName.typeNameFromString("foo/testee");

        @Override
        public CompletableFuture<Void> apply(Context context, Message message) throws Throwable {
            assertThat(message.isUtf8String()).isTrue();
            assertThat(message.asUtf8String()).isEqualTo(HELLO);
            Message outgoingMessage = MessageBuilder.fromMessage(message)
                    .withTargetAddress(COLLABORATOR, context.self().id())
                    .build();
            context.send(outgoingMessage);
            return context.done();
        }
    }
}