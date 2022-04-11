package com.github.f1xman.statefun.tsukuyomi.core;

import com.github.f1xman.statefun.tsukuyomi.core.capture.MessageCaptureFunction;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor
@Builder
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode
public class ModuleDefinition {

    FunctionDefinition functionUnderTest;
    @Singular
    Set<TypeName> collaborators;

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
                .collect(Collectors.joining(";"));
    }

    @RequiredArgsConstructor(staticName = "of")
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    @Getter
    @Builder
    @EqualsAndHashCode
    public static class FunctionDefinition {

        TypeName typeName;
        StatefulFunction instance;
        List<StateSetter<?>> stateSetters;

        public ValueSpec<?>[] getValueSpecs() {
            return stateSetters.stream()
                    .map(StateSetter::getValueSpec)
                    .toArray(ValueSpec[]::new);
        }
    }

}
