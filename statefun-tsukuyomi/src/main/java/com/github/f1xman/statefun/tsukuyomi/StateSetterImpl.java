package com.github.f1xman.statefun.tsukuyomi;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.ValueSpec;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class StateSetterImpl<T> implements StateSetter<T> {

    ValueSpec<T> spec;
    T value;

    @Override
    public ValueSpec<T> getValueSpec() {
        return spec;
    }
}
