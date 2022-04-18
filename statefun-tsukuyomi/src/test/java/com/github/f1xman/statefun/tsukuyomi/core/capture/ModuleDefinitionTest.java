package com.github.f1xman.statefun.tsukuyomi.core.capture;

import com.github.f1xman.statefun.tsukuyomi.core.capture.*;
import org.apache.flink.statefun.sdk.java.*;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleDefinitionTest {

    static final TypeName COLLABORATOR_1 = TypeName.typeNameFromString("foo/collaborator-1");
    static final TypeName COLLABORATOR_2 = TypeName.typeNameFromString("foo/collaborator-2");
    static final TypeName EGRESS_1 = TypeName.typeNameFromString("foo/egress-1");
    static final TypeName EGRESS_2 = TypeName.typeNameFromString("foo/egress-2");
    static final ValueSpec<String> VALUE_SPEC = ValueSpec.named("foo").withUtf8StringType();

    @Test
    void buildsStatefulFunctions() {
        StatefulFunction functionUnderTest = new FunctionUnderTest();
        List<StateSetter<?>> stateSetters = List.of(StateSetterImpl.of(VALUE_SPEC, null));
        ManagedStateFunctionWrapper expectedManagedStateWrapper = ManagedStateFunctionWrapper.of(functionUnderTest, stateSetters);
        ModuleDefinition.FunctionDefinition functionDefinition = ModuleDefinition.FunctionDefinition.builder()
                .typeName(FunctionUnderTest.TYPE)
                .instance(functionUnderTest)
                .stateSetters(stateSetters)
                .build();
        ModuleDefinition moduleDefinition = ModuleDefinition.builder()
                .functionUnderTest(functionDefinition)
                .collaborator(COLLABORATOR_1)
                .collaborator(COLLABORATOR_2)
                .build();

        StatefulFunctions statefulFunctions = moduleDefinition.toStatefulFunctions();

        assertThat(statefulFunctions.functionSpecs())
                .hasEntrySatisfying(FunctionUnderTest.TYPE, s -> {
                    assertThat(s.typeName()).isEqualTo(FunctionUnderTest.TYPE);
                    assertThat(s.supplier().get()).isEqualTo(expectedManagedStateWrapper);
                    assertThat(s.knownValues()).containsValue(VALUE_SPEC);
                })
                .hasEntrySatisfying(COLLABORATOR_1, s -> {
                    assertThat(s.typeName()).isEqualTo(COLLABORATOR_1);
                    assertThat(s.supplier().get()).isSameAs(MessageCaptureFunction.INSTANCE);
                })
                .hasEntrySatisfying(COLLABORATOR_2, s -> {
                    assertThat(s.typeName()).isEqualTo(COLLABORATOR_2);
                    assertThat(s.supplier().get()).isSameAs(MessageCaptureFunction.INSTANCE);
                });
    }

    @Test
    void generatesStringOfFunctionTypesSeparatedBySemicolon() {
        ModuleDefinition.FunctionDefinition functionDefinition = ModuleDefinition.FunctionDefinition.builder()
                .typeName(FunctionUnderTest.TYPE)
                .instance(new FunctionUnderTest())
                .build();
        ModuleDefinition moduleDefinition = ModuleDefinition.builder()
                .functionUnderTest(functionDefinition)
                .collaborator(COLLABORATOR_1)
                .build();
        String expected = FunctionUnderTest.TYPE.asTypeNameString() + ";" + COLLABORATOR_1.asTypeNameString();

        String actual = moduleDefinition.generateFunctionsString();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generatesStringOfEgressesSeparatedBySemicolon() {
        ModuleDefinition.FunctionDefinition functionDefinition = ModuleDefinition.FunctionDefinition.builder()
                .typeName(FunctionUnderTest.TYPE)
                .instance(new FunctionUnderTest())
                .build();
        ModuleDefinition moduleDefinition = ModuleDefinition.builder()
                .functionUnderTest(functionDefinition)
                .egress(EGRESS_1)
                .egress(EGRESS_2)
                .build();
        String expected = EGRESS_1.asTypeNameString() + ";" + EGRESS_2.asTypeNameString();

        String actual = moduleDefinition.generateEgressesString();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void returnsStateAccessor() {
        StatefulFunction functionUnderTest = new FunctionUnderTest();
        List<StateSetter<?>> stateSetters = List.of(StateSetterImpl.of(VALUE_SPEC, null));
        ModuleDefinition.FunctionDefinition functionDefinition = ModuleDefinition.FunctionDefinition.builder()
                .typeName(FunctionUnderTest.TYPE)
                .instance(functionUnderTest)
                .stateSetters(stateSetters)
                .build();
        ModuleDefinition moduleDefinition = ModuleDefinition.builder()
                .functionUnderTest(functionDefinition)
                .collaborator(COLLABORATOR_1)
                .collaborator(COLLABORATOR_2)
                .build();

        ManagedStateAccessor stateAccessor = moduleDefinition.getStateAccessor();

        assertThat(stateAccessor).isNotNull();
    }

    private static class FunctionUnderTest implements StatefulFunction {

        static TypeName TYPE = TypeName.typeNameFromString("foo/function-under-test");

        @Override
        public CompletableFuture<Void> apply(Context context, Message argument) throws Throwable {
            return null;
        }
    }

}