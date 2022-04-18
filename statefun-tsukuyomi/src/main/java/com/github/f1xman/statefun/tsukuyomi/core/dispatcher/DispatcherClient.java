package com.github.f1xman.statefun.tsukuyomi.core.dispatcher;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.SneakyThrows;

import java.util.Collection;

public interface DispatcherClient {
    @SneakyThrows
    void connect();

    void send(Envelope envelope);

    Collection<Envelope> getReceived();
}
