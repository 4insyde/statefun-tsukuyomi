package com.github.f1xman.statefun.tsukuyomi.core.capture;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.apache.flink.statefun.sdk.java.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor
@Builder
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode
@ToString
public class ModuleDefinition {

    FunctionDefinition functionUnderTest;
    @Singular
    Set<TypeName> collaborators;
    @Singular
    Set<TypeName> egresses;

    public StatefulFunctions toStatefulFunctions() {
        StatefulFunctionSpec functionUnderTestSpec = StatefulFunctionSpec
                .builder(functionUnderTest.getTypeName())
                .withSupplier(functionUnderTest::getInstance)
                .withValueSpecs(functionUnderTest.getValueSpecs())
                .build();
        Stream<StatefulFunctionSpec> collaboratorSpecs = getCollaborators().stream()
                .map(t -> StatefulFunctionSpec
                        .builder(t)
                        .withSupplier(() -> MessageCaptureFunction.INSTANCE)
                        .build()
                );
        StatefulFunctions statefulFunctions = new StatefulFunctions();
        Stream
                .concat(Stream.of(functionUnderTestSpec), collaboratorSpecs)
                .forEach(statefulFunctions::withStatefulFunction);
        return statefulFunctions;
    }

    public String generateFunctionsString() {
        return Stream.concat(
                        Stream.of(functionUnderTest.getTypeName()),
                        collaborators.stream()
                )
                .map(TypeName::asTypeNameString)
                .collect(joining(";"));
    }

    public ManagedStateAccessor getStateAccessor() {
        return functionUnderTest.getStateAccessor();
    }

    public String generateEgressesString() {
        return egresses.stream()
                .map(TypeName::asTypeNameString)
                .collect(joining(";"));
    }

    @FieldDefaults(level = PRIVATE, makeFinal = true)
    @EqualsAndHashCode
    public static class FunctionDefinition {

        @Getter
        TypeName typeName;
        StatefulFunction instance;
        List<StateSetter<?>> stateSetters;
        @NonFinal
        ManagedStateFunctionWrapper wrapper;

        @Builder
        private FunctionDefinition(TypeName typeName, StatefulFunction instance, List<StateSetter<?>> stateSetters) {
            this.typeName = typeName;
            this.instance = instance;
            this.stateSetters = stateSetters;
        }

        public ValueSpec<?>[] getValueSpecs() {
            return stateSetters.stream()
                    .map(StateSetter::getValueSpec)
                    .toArray(ValueSpec[]::new);
        }

        public StatefulFunction getInstance() {
            return wrapper();
        }

        private ManagedStateFunctionWrapper wrapper() {
            if (wrapper == null) {
                wrapper = ManagedStateFunctionWrapper.of(instance, stateSetters);
            }
            return wrapper;
        }

        public ManagedStateAccessor getStateAccessor() {
            return wrapper();
        }
    }

}
