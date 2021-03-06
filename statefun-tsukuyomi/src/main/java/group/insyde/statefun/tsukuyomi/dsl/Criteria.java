package group.insyde.statefun.tsukuyomi.dsl;

import group.insyde.statefun.tsukuyomi.core.capture.Envelope;
import group.insyde.statefun.tsukuyomi.core.validation.EnvelopeCriterion;
import group.insyde.statefun.tsukuyomi.core.validation.StateCriterion;
import lombok.NoArgsConstructor;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.hamcrest.Matcher;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Criteria {

    public static <T> CriterionFactory state(ValueSpec<T> spec, Matcher<T> matcher) {
        if (spec == null) {
            throw new NullValueSpecException("ValueSpec of a verifying state is null");
        }
        if (matcher == null) {
            throw new NullStateMatcherException(
                    "Matcher cannot be null. Either use org.hamcrest.Matchers.*() or implement a custom one");
        }
        return order -> StateCriterion.of(spec, matcher);
    }

    public static CriterionFactory sendsInOrder(Envelope expected) {
        if (expected == null) {
            throw new NullExpectedEnvelopeException(
                    "Expected envelope cannot be null. Use Envelope.builder() to build a message");
        }
        return order -> EnvelopeCriterion.ofOrdered(order, expected);
    }

    public static CriterionFactory sends(Envelope expected) {
        if (expected == null) {
            throw new NullExpectedEnvelopeException(
                    "Expected envelope cannot be null. Use Envelope.builder() to build a message");
        }
        return order -> EnvelopeCriterion.of(expected);
    }

}
