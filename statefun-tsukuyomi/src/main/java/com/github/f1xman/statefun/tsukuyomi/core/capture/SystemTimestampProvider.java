package com.github.f1xman.statefun.tsukuyomi.core.capture;

public class SystemTimestampProvider implements TimestampProvider {
    @Override
    public Long currentTimestamp() {
        return System.nanoTime();
    }
}
