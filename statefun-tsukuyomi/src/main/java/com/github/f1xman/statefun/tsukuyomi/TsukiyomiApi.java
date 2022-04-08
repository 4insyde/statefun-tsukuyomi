package com.github.f1xman.statefun.tsukuyomi;

import com.github.f1xman.statefun.tsukuyomi.capture.Envelope;

import java.util.Collection;

public interface TsukiyomiApi {
    void send(Envelope envelope);

    Collection<Envelope> getReceived();
}
