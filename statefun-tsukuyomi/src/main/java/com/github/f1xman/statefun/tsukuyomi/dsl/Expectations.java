package com.github.f1xman.statefun.tsukuyomi.dsl;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.validation.ChangeMatcher;
import com.github.f1xman.statefun.tsukuyomi.core.validation.ExpectMessage;
import com.github.f1xman.statefun.tsukuyomi.core.validation.ExpectState;
import com.github.f1xman.statefun.tsukuyomi.core.validation.Target;
import lombok.NoArgsConstructor;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.hamcrest.Matcher;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Expectations {

    public static <T> ChangeMatcher expectState(ValueSpec<T> spec, Matcher<T> matcher) {
        if (spec == null) {
            throw new NullValueSpecException("ValueSpec of a verifying state is null");
        }
        if (matcher == null) {
            throw new NullStateMatcherException(
                    "Matcher cannot be null. Either use org.hamcrest.Matchers.*() or implement a custom one");
        }
        return ExpectState.of(spec, matcher);
    }

    public static ChangeMatcher expectMessage(Envelope expected) {
        if (expected == null) {
            throw new NullExpectedEnvelopeException(
                    "Expected envelope cannot be null. Use Envelope.builder() to build a message");
        }
        return ExpectMessage.of(expected, Target.Type.FUNCTION);
    }

    public static ChangeMatcher expectEgressMessage(Envelope expected) {
        if (expected == null) {
            throw new NullExpectedEnvelopeException(
                    "Expected envelope to egress cannot be null. Use Envelope.builder() to build a message");
        }
        return ExpectMessage.of(expected, Target.Type.EGRESS);
    }
}
