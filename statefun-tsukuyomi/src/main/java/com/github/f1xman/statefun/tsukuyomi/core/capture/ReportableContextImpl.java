package com.github.f1xman.statefun.tsukuyomi.core.capture;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.Address;
import org.apache.flink.statefun.sdk.java.AddressScopedStorage;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.message.EgressMessage;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;
import org.apache.flink.statefun.sdk.java.message.Message;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.newSetFromMap;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "spyOn")
@FieldDefaults(level = PRIVATE, makeFinal = true)
@EqualsAndHashCode
public class ReportableContextImpl implements ReportableContext {

    AtomicInteger numberOfOutgoingMessages = new AtomicInteger();
    Set<String> cancellationTokens = newSetFromMap(new ConcurrentHashMap<>());
    List<Envelope> envelopes = Collections.synchronizedList(new ArrayList<>());
    @NonNull
    Context context;

    @Override
    public void report() {
        Envelope envelope = Envelope.builder()
                .toEgress(Egresses.CAPTURED_MESSAGES)
                .data(InvocationReport.TYPE, InvocationReport.of(getNumberOfOutgoingMessages(), envelopes))
                .build();
        EgressMessage message = EgressMessageBuilder.forEgress(Egresses.CAPTURED_MESSAGES)
                .withCustomType(Envelope.TYPE, envelope)
                .build();
        context.send(message);
    }

    @Override
    public Address self() {
        return context.self();
    }

    @Override
    public Optional<Address> caller() {
        return context.caller();
    }

    @Override
    public void send(Message message) {
        Envelope envelope = Envelope.fromMessage(self(), message);
        envelopes.add(envelope);
        context.send(message);
        numberOfOutgoingMessages.incrementAndGet();
    }

    @Override
    public void send(EgressMessage message) {
        Envelope envelope = Envelope.fromMessage(message);
        envelopes.add(envelope);
        context.send(message);
        numberOfOutgoingMessages.incrementAndGet();
    }

    @Override
    public void sendAfter(Duration duration, Message message) {
        context.sendAfter(duration, message);
        numberOfOutgoingMessages.incrementAndGet();
    }

    @Override
    public void sendAfter(Duration duration, String cancellationToken, Message message) {
        context.sendAfter(duration, cancellationToken, message);
        cancellationTokens.add(cancellationToken);
    }

    @Override
    public void cancelDelayedMessage(String cancellationToken) {
        context.cancelDelayedMessage(cancellationToken);
        cancellationTokens.remove(cancellationToken);
    }

    @Override
    public AddressScopedStorage storage() {
        return context.storage();
    }

    public Integer getNumberOfOutgoingMessages() {
        return numberOfOutgoingMessages.get() + cancellationTokens.size();
    }
}
