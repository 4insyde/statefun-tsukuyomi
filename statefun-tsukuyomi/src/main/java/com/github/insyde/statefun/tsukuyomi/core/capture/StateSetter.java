package com.github.insyde.statefun.tsukuyomi.core.capture;

import org.apache.flink.statefun.sdk.java.AddressScopedStorage;
import org.apache.flink.statefun.sdk.java.ValueSpec;

public interface StateSetter<T> {

    ValueSpec<T> getValueSpec();

    void setStateValue(AddressScopedStorage storage);
}
