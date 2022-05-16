package com.github.insyde.statefun.tsukuyomi.core.capture;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.StatefulFunctionSpec;
import org.apache.flink.statefun.sdk.java.TypeName;

import java.util.Set;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
@EqualsAndHashCode
class CollaboratorSet {

    Set<TypeName> typeNames;

    Stream<StatefulFunctionSpec> createSpecsStream() {
        return typeNames.stream()
                .map(t -> StatefulFunctionSpec
                        .builder(t)
                        .withSupplier(() -> MessageCaptureFunction.INSTANCE)
                        .build()
                );
    }

    Stream<TypeName> stream() {
        return typeNames.stream();
    }
}
