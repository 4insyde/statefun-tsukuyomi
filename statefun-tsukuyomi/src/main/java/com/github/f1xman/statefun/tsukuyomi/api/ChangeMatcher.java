package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.capture.Envelope;

import java.util.Collection;
import java.util.function.Supplier;

public interface ChangeMatcher {

    void match(int order, Supplier<Collection<Envelope>> receivedSupplier);
}
