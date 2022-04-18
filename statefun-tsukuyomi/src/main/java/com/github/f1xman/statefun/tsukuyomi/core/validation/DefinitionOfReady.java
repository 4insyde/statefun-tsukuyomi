package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
class DefinitionOfReady {

    AtomicInteger expectedEnvelopes = new AtomicInteger();
    AtomicBoolean updatedStateRequired = new AtomicBoolean();
    TsukuyomiApi tsukuyomiApi;

    public void incrementExpectedEnvelopes() {
        expectedEnvelopes.incrementAndGet();
    }

    public void await() {
        if (expectedEnvelopes.get() > 0) {
            Waiter waiter = new Waiter(() -> {
                Collection<Envelope> receivedEnvelopes = tsukuyomiApi.getReceived();
                return receivedEnvelopes.size() == expectedEnvelopes.intValue();
            });
            waiter.await();
        }
        if (updatedStateRequired.get()) {
            Waiter waiter = new Waiter(tsukuyomiApi::isStateUpdated);
            waiter.await();
        }
    }

    public void requireUpdatedState() {
        updatedStateRequired.set(true);
    }

    @RequiredArgsConstructor(staticName = "of")
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    private static class Waiter {

        Supplier<Boolean> matchingValueSupplier;

        void await() {
            while (!Thread.interrupted() && !matchingValueSupplier.get()) {
                Thread.onSpinWait();
            }
        }

    }
}
