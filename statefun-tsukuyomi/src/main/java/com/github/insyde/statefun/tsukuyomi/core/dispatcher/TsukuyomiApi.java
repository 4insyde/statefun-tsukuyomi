package com.github.insyde.statefun.tsukuyomi.core.dispatcher;

import com.github.insyde.statefun.tsukuyomi.core.capture.Envelope;
import com.github.insyde.statefun.tsukuyomi.core.capture.InvocationReport;
import com.github.insyde.statefun.tsukuyomi.core.capture.ManagedStateAccessor;

import java.util.Collection;
import java.util.Optional;

public interface TsukuyomiApi {
    void send(Envelope envelope);

    Collection<Envelope> getReceived();

    ManagedStateAccessor getStateAccessor();

    boolean isStateUpdated();

    boolean isActive();

    Optional<InvocationReport> getInvocationReport();
}
