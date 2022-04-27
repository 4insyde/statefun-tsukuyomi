package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class AbstractExpectMessage implements MessageMatcher {
    @Override
    public Target getTarget() {
        String type = getTo().getType();
        TypeName typeName = TypeName.typeNameFromString(type);
        return Target.of(typeName, getTargetType());
    }

    @NotNull
    protected abstract Envelope.NodeAddress getTo();

    protected abstract Target.Type getTargetType();
}
