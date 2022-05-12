package com.github.f1xman.statefun.tsukuyomi.core.capture;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.statefun.sdk.java.Address;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.message.EgressMessage;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;
import org.apache.flink.statefun.sdk.java.message.Message;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
class MessageCaptureFunction implements StatefulFunction {

    public static final MessageCaptureFunction INSTANCE = new MessageCaptureFunction();

    @Override
    public CompletableFuture<Void> apply(Context context, Message message) {
        Address caller = context.caller().orElseThrow(() -> new IllegalStateException("Caller is missing"));
        Address self = context.self();
        log.info("Captured outgoing message {} from function {} to function {}", message.valueTypeName(), caller, self);
        Envelope envelope = Envelope.builder()
                .from(caller.type(), caller.id())
                .toFunction(self.type(), self.id())
                .data(Envelope.Data.of(
                        message.valueTypeName().asTypeNameString(),
                        Base64.getEncoder().encodeToString(message.rawValue().toByteArray())
                ))
                .build();
        EgressMessage egressMessage = EgressMessageBuilder.forEgress(Egresses.CAPTURED_MESSAGES)
                .withCustomType(Envelope.TYPE, envelope)
                .build();
        context.send(egressMessage);
        return context.done();
    }
}
