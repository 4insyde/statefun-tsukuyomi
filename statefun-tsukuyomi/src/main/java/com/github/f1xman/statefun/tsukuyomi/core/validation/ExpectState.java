package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.ManagedStateAccessor;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.hamcrest.Matcher;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ExpectState<T> implements StateMatcherOld {

    ValueSpec<T> spec;
    Matcher<T> matcher;

    @Override
    public void match(TsukuyomiApi tsukuyomi) {
        ManagedStateAccessor stateAccessor = tsukuyomi.getStateAccessor();
        Optional<T> value = stateAccessor.getStateValue(spec);
        assertThat(value.orElse(null), matcher);
    }
}
