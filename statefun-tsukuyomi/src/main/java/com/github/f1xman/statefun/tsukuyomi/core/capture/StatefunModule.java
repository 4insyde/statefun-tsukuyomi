package com.github.f1xman.statefun.tsukuyomi.core.capture;

import com.github.f1xman.statefun.tsukuyomi.util.Distinct;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.apache.flink.statefun.sdk.java.StatefulFunctionSpec;
import org.apache.flink.statefun.sdk.java.StatefulFunctions;
import org.apache.flink.statefun.sdk.java.TypeName;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode
@ToString
public class StatefunModule {

    FunctionDefinition functionUnderTest;
    CollaboratorSet collaborators;
    Set<TypeName> egresses;
    @NonFinal
    StatefunServer server;

    @Builder
    public StatefunModule(
            FunctionDefinition functionUnderTest,
            @Singular(ignoreNullCollections = true) Set<TypeName> collaborators,
            @Singular(ignoreNullCollections = true) Set<TypeName> egresses) {
        this.functionUnderTest = functionUnderTest;
        this.collaborators = CollaboratorSet.of(collaborators);
        this.egresses = egresses;
    }

    public String generateFunctionsString() {
        return Stream.concat(
                        Stream.of(functionUnderTest.getTypeName()),
                        collaborators.stream()
                )
                .map(TypeName::asTypeNameString)
                .collect(joining(";"));
    }

    public String generateEgressesString() {
        return egresses.stream()
                .map(TypeName::asTypeNameString)
                .collect(joining(";"));
    }

    public ManagedStateAccessor getStateAccessor() {
        return functionUnderTest.getStateAccessor();
    }

    public void start(StatefunServer server) {
        this.server = server;
        StatefulFunctionSpec functionUnderTestSpec = functionUnderTest.createSpec();
        Stream<StatefulFunctionSpec> collaboratorSpecs = collaborators.createSpecsStream();
        StatefulFunctions statefulFunctions = new StatefulFunctions();
        Stream.concat(
                        Stream.of(functionUnderTestSpec),
                        collaboratorSpecs
                )
                .filter(Distinct.byKey(StatefulFunctionSpec::typeName))
                .forEach(statefulFunctions::withStatefulFunction);
        this.server.start(statefulFunctions);
    }

    public void stop() {
        if (this.server != null) {
            this.server.stop();
        }
    }

    public Integer getPort() {
        if (server == null) {
            throw new StatefunModuleNotStartedException("Port is not available until StatefunModule is started");
        }
        return server.getPort();
    }

}
