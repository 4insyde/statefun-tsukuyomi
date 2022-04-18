package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.ManagedStateAccessor;

import java.util.Collection;

public interface TsukuyomiApi {
    void send(Envelope envelope);

    Collection<Envelope> getReceived();

    ManagedStateAccessor getStateAccessor();

    boolean isStateUpdated();
}
