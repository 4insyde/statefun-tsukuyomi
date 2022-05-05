package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class EnvelopeCriterion implements Criterion {

    int order;
    Envelope envelope;

    public static EnvelopeCriterion ordered(int index, Envelope envelope) {
        return new EnvelopeCriterion(index, envelope);
    }

    public static EnvelopeCriterion unordered(Envelope envelope) {
        return new EnvelopeCriterion(Integer.MIN_VALUE, envelope);
    }

    public boolean isOrdered() {
        return order >= 0;
    }
}
