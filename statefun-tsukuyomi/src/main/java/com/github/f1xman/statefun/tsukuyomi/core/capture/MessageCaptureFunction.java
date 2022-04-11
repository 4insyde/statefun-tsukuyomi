package com.github.f1xman.statefun.tsukuyomi.core.capture;

import lombok.RequiredArgsConstructor;
import org.apache.flink.statefun.sdk.java.Address;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.message.EgressMessage;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;
import org.apache.flink.statefun.sdk.java.message.Message;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class MessageCaptureFunction implements StatefulFunction {

    public static final MessageCaptureFunction INSTANCE = new MessageCaptureFunction();

    @Override
    public CompletableFuture<Void> apply(Context context, Message message) {
        Address caller = context.caller().orElseThrow(() -> new IllegalStateException("Caller is missing"));
        Address self = context.self();
        EgressMessage egressMessage = EgressMessageBuilder.forEgress(Egresses.CAPTURED_MESSAGES)
                .withCustomType(Envelope.TYPE, new Envelope(
                        Envelope.NodeAddress.of(caller.type().asTypeNameString(), caller.id()),
                        Envelope.NodeAddress.of(self.type().asTypeNameString(), self.id()),
                        Envelope.Data.of(
                                message.valueTypeName().asTypeNameString(),
                                Base64.getEncoder().encodeToString(message.rawValue().toByteArray())
                        )
                ))
                .build();
        context.send(egressMessage);
        return context.done();
    }
}
