package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EnvelopeCriterion implements Criterion {

    @Getter
    int order;
    @Getter
    Envelope envelope;

    public static EnvelopeCriterion ofOrdered(int index, Envelope envelope) {
        return new EnvelopeCriterion(index, envelope);
    }

    public static EnvelopeCriterion of(Envelope envelope) {
        return new EnvelopeCriterion(Integer.MIN_VALUE, envelope);
    }

    public boolean isOrdered() {
        return order >= 0;
    }

    public Target getTarget() {
        Envelope.NodeAddress to = envelope.getTo();
        return to.toTarget();
    }
}
