package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.InvocationReport;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "getFrom")
@FieldDefaults(level = PRIVATE, makeFinal = true)
class DefinitionOfReady {

    TsukuyomiApi tsukuyomiApi;

    public void incrementExpectedEnvelopes() {
    }

    public void await() {
        Waiter waiter = new Waiter(() -> {
            Collection<Envelope> receivedEnvelopes = new ArrayList<>(tsukuyomiApi.getReceived());
            Optional<InvocationReport> report = receivedEnvelopes.stream()
                    .filter(e -> e.is(InvocationReport.TYPE))
                    .map(e -> e.extractData(InvocationReport.TYPE))
                    .findAny();
            Integer expectedSize = report.map(InvocationReport::getOutgoingMessagesCount)
                    .map(c -> c + 1)
                    .orElse(Integer.MAX_VALUE);
            return receivedEnvelopes.size() == expectedSize;
        });
        waiter.await(tsukuyomiApi::isActive);
    }

    public void requireUpdatedState() {
    }

    @RequiredArgsConstructor(staticName = "of")
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    private static class Waiter {

        Supplier<Boolean> matchingValueSupplier;

        void await(Supplier<Boolean> keepRunning) {
            while (
                    !Thread.currentThread().isInterrupted()
                            && !matchingValueSupplier.get()
                            && keepRunning.get()
            ) {
                Thread.onSpinWait();
            }
        }

    }
}
