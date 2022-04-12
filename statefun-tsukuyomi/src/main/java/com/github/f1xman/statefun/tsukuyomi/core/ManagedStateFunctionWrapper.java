package com.github.f1xman.statefun.tsukuyomi.core;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.AddressScopedStorage;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.message.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
@EqualsAndHashCode
public class ManagedStateFunctionWrapper implements StatefulFunction {

    StatefulFunction function;
    List<StateSetter<?>> stateSetters;

    @Override
    @SneakyThrows
    public CompletableFuture<Void> apply(Context context, Message message) {
        AddressScopedStorage storage = context.storage();
        stateSetters.forEach(s -> s.setStateValue(storage));
        return function.apply(context, message);
    }
}
