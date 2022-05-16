package com.github.insyde.statefun.tsukuyomi.core;

import com.github.insyde.statefun.tsukuyomi.core.capture.Envelope;
import com.github.insyde.statefun.tsukuyomi.core.capture.FunctionDefinition;
import com.github.insyde.statefun.tsukuyomi.core.capture.StatefunModule;
import com.github.insyde.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import com.github.insyde.statefun.tsukuyomi.core.validation.TsukuyomiManagerImpl;
import com.github.insyde.statefun.tsukuyomi.testutil.IntegrationTest;
import com.github.insyde.statefun.tsukuyomi.testutil.ServerUtils;
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
            FunctionDefinition functionDefinition = FunctionDefinition.builder()
                    .typeName(Testee.TYPE)
                    .instance(testee)
                    .stateSetters(List.of())
                    .build();
            StatefunModule statefunModule = StatefunModule.builder()
                    .functionUnderTest(functionDefinition)
                    .collaborator(COLLABORATOR)
                    .egress(EGRESS)
                    .build();
            TsukuyomiApi client = tsukuyomiManager.start(statefunModule);
            Envelope envelope = Envelope.builder()
                    .from(COLLABORATOR, FUNCTION_ID)
                    .toFunction(Testee.TYPE, FUNCTION_ID)
                    .data(Types.stringType(), HELLO)
                    .build();
            Envelope expectedToFunction = envelope.toBuilder()
                    .from(Testee.TYPE, FUNCTION_ID)
                    .toFunction(COLLABORATOR, FUNCTION_ID)
                    .build();
            Envelope expectedToEgress = envelope.toBuilder()
                    .from(null)
                    .toEgress(EGRESS)
                    .build();

            client.send(envelope);
            await().atMost(Duration.ofMinutes(1)).until(() -> client.getReceived().size() >= 2);
            assertThat(client.getReceived()).contains(expectedToFunction, expectedToEgress);

            tsukuyomiManager.stop();
        });
    }

    @Test
    void stopsTsukuyomiIfFunctionThrowsException() {
        Assertions.assertTimeoutPreemptively(Duration.ofMinutes(1), () -> {
            TsukuyomiManagerImpl tsukuyomiManager = new TsukuyomiManagerImpl();
            FunctionDefinition functionDefinition = FunctionDefinition.builder()
                    .typeName(Testee.TYPE)
                    .instance((c, m) -> {
                        throw new RuntimeException();
                    })
                    .stateSetters(List.of())
                    .build();
            StatefunModule statefunModule = StatefunModule.builder()
                    .functionUnderTest(functionDefinition)
                    .build();
            TsukuyomiApi client = tsukuyomiManager.start(statefunModule);
            Envelope envelope = Envelope.builder()
                    .from(COLLABORATOR, FUNCTION_ID)
                    .toFunction(Testee.TYPE, FUNCTION_ID)
                    .data(Types.stringType(), HELLO)
                    .build();

            client.send(envelope);

            await().until(() -> !ServerUtils.isStatefunModuleRunning(statefunModule));
        });
    }

    @Test
    void startReturnsActiveTsukuyomiApi() {
        Assertions.assertTimeoutPreemptively(Duration.ofMinutes(1), () -> {
            TsukuyomiManagerImpl tsukuyomiManager = new TsukuyomiManagerImpl();
            FunctionDefinition functionDefinition = FunctionDefinition.builder()
                    .typeName(Testee.TYPE)
                    .instance((c, m) -> {
                        throw new RuntimeException();
                    })
                    .stateSetters(List.of())
                    .build();
            StatefunModule statefunModule = StatefunModule.builder()
                    .functionUnderTest(functionDefinition)
                    .build();
            TsukuyomiApi client = tsukuyomiManager.start(statefunModule);

            boolean actualActive = client.isActive();

            assertThat(actualActive).isTrue();
        });
    }

    @Test
    void stopDeactivatesTsukuyomiApi() {
        Assertions.assertTimeoutPreemptively(Duration.ofMinutes(1), () -> {
            TsukuyomiManagerImpl tsukuyomiManager = new TsukuyomiManagerImpl();
            FunctionDefinition functionDefinition = FunctionDefinition.builder()
                    .typeName(Testee.TYPE)
                    .instance((c, m) -> {
                        throw new RuntimeException();
                    })
                    .stateSetters(List.of())
                    .build();
            StatefunModule statefunModule = StatefunModule.builder()
                    .functionUnderTest(functionDefinition)
                    .build();
            TsukuyomiApi client = tsukuyomiManager.start(statefunModule);
            tsukuyomiManager.stop();

            boolean actualActive = client.isActive();

            assertThat(actualActive).isFalse();
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