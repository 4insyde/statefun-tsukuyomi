package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class AbstractExpectMessage implements ChangeMatcher {
    @Override
    public Optional<Target> getTarget() {
        return Optional.of(getTo())
                .map(Envelope.NodeAddress::getType)
                .map(TypeName::typeNameFromString)
                .map(t -> Target.of(t, getTargetType()));
    }

    @NotNull
    protected abstract Envelope.NodeAddress getTo();

    protected abstract Target.Type getTargetType();
}
