package com.github.f1xman.statefun.tsukuyomi.core;

import com.github.f1xman.statefun.tsukuyomi.capture.Envelope;

import java.util.Collection;

public interface TsukiyomiApi {
    void send(Envelope envelope);

    Collection<Envelope> getReceived();
}
