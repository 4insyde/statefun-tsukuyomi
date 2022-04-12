package com.github.f1xman.statefun.tsukuyomi.core;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Collection;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class DispatcherBasedTsukuyomi implements TsukuyomiApi {

    DispatcherClient client;
    ManagedStateAccessor stateAccessor;

    @Override
    public void send(Envelope envelope) {
        client.send(envelope);
    }

    @Override
    public Collection<Envelope> getReceived() {
        return client.getReceived();
    }

    @Override
    public ManagedStateAccessor getStateAccessor() {
        return stateAccessor;
    }
}
