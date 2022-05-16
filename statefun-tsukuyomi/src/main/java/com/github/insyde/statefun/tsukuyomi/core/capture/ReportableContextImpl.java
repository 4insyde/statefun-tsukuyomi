package com.github.insyde.statefun.tsukuyomi.core.capture;

import lombok.EqualsAndHashCode;
import lombok.Getter;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "spyOn")
@FieldDefaults(level = PRIVATE, makeFinal = true)
@EqualsAndHashCode
public class ReportableContextImpl implements ReportableContext {

    AtomicInteger numberOfOutgoingMessages = new AtomicInteger();
    List<EnvelopeEntry> envelopes = Collections.synchronizedList(new ArrayList<>());
    @NonNull
    Context context;

    @Override
    public void report() {
        Envelope envelope = Envelope.builder()
                .toEgress(Egresses.CAPTURED_MESSAGES)
                .data(InvocationReport.TYPE, InvocationReport.of(extractEnvelopes()))
                .build();
        EgressMessage message = EgressMessageBuilder.forEgress(Egresses.CAPTURED_MESSAGES)
                .withCustomType(Envelope.TYPE, envelope)
                .build();
        context.send(message);
    }

    private List<Envelope> extractEnvelopes() {
        return envelopes.stream()
                .map(EnvelopeEntry::getEnvelope)
                .collect(toUnmodifiableList());
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
        envelopes.add(EnvelopeEntry.permanent(envelope));
        context.send(message);
        numberOfOutgoingMessages.incrementAndGet();
    }

    @Override
    public void send(EgressMessage message) {
        Envelope envelope = Envelope.fromMessage(message);
        envelopes.add(EnvelopeEntry.permanent(envelope));
        context.send(message);
        numberOfOutgoingMessages.incrementAndGet();
    }

    @Override
    public void sendAfter(Duration duration, Message message) {
        context.sendAfter(duration, message);
        numberOfOutgoingMessages.incrementAndGet();
        Envelope envelope = Envelope.fromMessage(self(), message).toBuilder()
                .delay(duration)
                .build();
        envelopes.add(EnvelopeEntry.permanent(envelope));
    }

    @Override
    public void sendAfter(Duration duration, String cancellationToken, Message message) {
        context.sendAfter(duration, cancellationToken, message);
        Envelope envelope = Envelope.fromMessage(self(), message).toBuilder()
                .delay(duration)
                .build();
        envelopes.add(EnvelopeEntry.cancellable(envelope, cancellationToken));
    }

    @Override
    public void cancelDelayedMessage(String cancellationToken) {
        context.cancelDelayedMessage(cancellationToken);
        envelopes.removeIf(e -> cancellationToken.equals(e.cancellationToken));
    }

    @Override
    public AddressScopedStorage storage() {
        return context.storage();
    }

    public Integer getNumberOfOutgoingMessages() {
        return envelopes.size();
    }

    @RequiredArgsConstructor(access = PRIVATE)
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    @EqualsAndHashCode
    private static class EnvelopeEntry {

        @Getter
        Envelope envelope;
        String cancellationToken;

        static EnvelopeEntry permanent(Envelope envelope) {
            return new EnvelopeEntry(envelope, null);
        }

        static EnvelopeEntry cancellable(Envelope envelope, String cancellationToken) {
            return new EnvelopeEntry(envelope, cancellationToken);
        }

    }
}
