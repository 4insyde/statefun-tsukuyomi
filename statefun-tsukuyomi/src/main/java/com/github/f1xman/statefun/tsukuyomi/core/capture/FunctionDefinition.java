package com.github.f1xman.statefun.tsukuyomi.core.capture;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.StatefulFunctionSpec;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.ValueSpec;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE, makeFinal = true)
@EqualsAndHashCode
public class FunctionDefinition {

    @Getter
    TypeName typeName;
    ManagedStateFunctionWrapper wrapper;
    List<StateSetter<?>> stateSetters;

    @Builder
    private FunctionDefinition(TypeName typeName, StatefulFunction instance, @Singular List<StateSetter<?>> stateSetters) {
        this.typeName = typeName;
        this.wrapper = ManagedStateFunctionWrapper.of(instance, stateSetters);
        this.stateSetters = stateSetters;
    }

    private ValueSpec<?>[] getValueSpecs() {
        return stateSetters.stream()
                .map(StateSetter::getValueSpec)
                .toArray(ValueSpec[]::new);
    }

    public ManagedStateAccessor getStateAccessor() {
        return wrapper;
    }

    StatefulFunctionSpec createSpec() {
        NextInvocationsInterceptor interceptor = NextInvocationsInterceptor.of(
                wrapper,
                MessageCaptureFunction.INSTANCE
        );
        return StatefulFunctionSpec
                .builder(getTypeName())
                .withSupplier(() -> interceptor)
                .withValueSpecs(getValueSpecs())
                .build();
    }
}
