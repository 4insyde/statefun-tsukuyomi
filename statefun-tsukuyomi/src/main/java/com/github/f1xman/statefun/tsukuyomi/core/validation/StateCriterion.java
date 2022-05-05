package com.github.f1xman.statefun.tsukuyomi.core.validation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.hamcrest.Matcher;

import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class StateCriterion implements Criterion {

    ValueSpec<Object> valueSpec;
    Matcher<Object> matcher;

    public static <T > StateCriterion of(ValueSpec<T> valueSpec, Matcher<T> matcher) {
        return new StateCriterion((ValueSpec<Object>) valueSpec, (Matcher<Object>) matcher);
    }

}
