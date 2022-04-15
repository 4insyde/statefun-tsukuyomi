package com.github.f1xman.statefun.tsukuyomi.core;

import org.apache.flink.statefun.sdk.java.ValueSpec;

import java.util.Optional;

public interface ManagedStateAccessor {
    <T> Optional<T> getStateValue(ValueSpec<T> spec);

    boolean isStateUpdated();
}
