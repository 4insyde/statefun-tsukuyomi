package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.capture.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.function.Supplier;

import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
class ExpectMessage implements ChangeMatcher {

    Matcher<Envelope> matcher;

    @Override
    public void match(int order, Supplier<Collection<Envelope>> receivedSupplier) {
        Envelope envelope = getEnvelope(order, receivedSupplier);
        assertThat(envelope, matcher);
    }

    private Envelope getEnvelope(int order, Supplier<Collection<Envelope>> receivedSupplier) {
        Envelope envelope = null;
        while (envelope == null && !Thread.interrupted()) {
            Collection<Envelope> received = receivedSupplier.get();
            if (!received.isEmpty() && received.size() >= order) {
                Envelope[] envelopes = received.toArray(Envelope[]::new);
                envelope = envelopes[order];
            } else {
                Thread.onSpinWait();
            }
        }
        return envelope;
    }
}
