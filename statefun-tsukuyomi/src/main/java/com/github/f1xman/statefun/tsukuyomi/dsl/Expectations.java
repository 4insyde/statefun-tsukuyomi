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
        return ExpectState.of(spec, matcher);
    }

    public static ChangeMatcher expectMessage(Envelope expected, Target.Type targetType) {
        return ExpectMessage.of(expected, targetType);
    }
}
