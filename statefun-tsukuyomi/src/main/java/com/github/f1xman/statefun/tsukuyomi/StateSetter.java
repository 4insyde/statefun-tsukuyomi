package com.github.f1xman.statefun.tsukuyomi;

import org.apache.flink.statefun.sdk.java.ValueSpec;

public interface StateSetter<T> {

    ValueSpec<T> getValueSpec();
}
