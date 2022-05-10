package com.github.f1xman.statefun.tsukuyomi.dsl;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.validation.EnvelopeCriterion;
import com.github.f1xman.statefun.tsukuyomi.core.validation.StateCriterion;
import lombok.NoArgsConstructor;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.hamcrest.Matcher;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Criteria {

    public static <T> CriterionFactory expectState(ValueSpec<T> spec, Matcher<T> matcher) {
        if (spec == null) {
            throw new NullValueSpecException("ValueSpec of a verifying state is null");
        }
        if (matcher == null) {
            throw new NullStateMatcherException(
                    "Matcher cannot be null. Either use org.hamcrest.Matchers.*() or implement a custom one");
        }
        return order -> StateCriterion.of(spec, matcher);
    }

    public static CriterionFactory expectMessageInExactOrder(Envelope expected) {
        if (expected == null) {
            throw new NullExpectedEnvelopeException(
                    "Expected envelope cannot be null. Use Envelope.builder() to build a message");
        }
        return order -> EnvelopeCriterion.toFunction(order, expected);
    }

    public static CriterionFactory expectEgressMessageInExactOrder(Envelope expected) {
        if (expected == null) {
            throw new NullExpectedEnvelopeException(
                    "Expected envelope to egress cannot be null. Use Envelope.builder() to build a message");
        }
        return order -> EnvelopeCriterion.toEgress(order, expected);
    }

    public static CriterionFactory expectMessageInAnyOrder(Envelope expected) {
        if (expected == null) {
            throw new NullExpectedEnvelopeException(
                    "Expected envelope cannot be null. Use Envelope.builder() to build a message");
        }
        return order -> EnvelopeCriterion.toFunction(expected);
    }

    public static CriterionFactory expectEgressMessageInAnyOrder(Envelope expected) {
        if (expected == null) {
            throw new NullExpectedEnvelopeException(
                    "Expected envelope to egress cannot be null. Use Envelope.builder() to build a message");
        }
        return order -> EnvelopeCriterion.toEgress(expected);
    }
}
