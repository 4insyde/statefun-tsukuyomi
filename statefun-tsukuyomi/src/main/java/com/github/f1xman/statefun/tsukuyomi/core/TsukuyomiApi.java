package com.github.f1xman.statefun.tsukuyomi.core;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;

import java.util.Collection;

public interface TsukuyomiApi {
    void send(Envelope envelope);

    Collection<Envelope> getReceived();
}
