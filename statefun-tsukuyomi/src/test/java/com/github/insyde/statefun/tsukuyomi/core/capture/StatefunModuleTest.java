package com.github.insyde.statefun.tsukuyomi.core.capture;

import org.apache.flink.statefun.sdk.java.*;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatefunModuleTest {

    static final TypeName COLLABORATOR_1 = TypeName.typeNameFromString("foo/collaborator-1");
    static final TypeName COLLABORATOR_2 = TypeName.typeNameFromString("foo/collaborator-2");
    static final TypeName EGRESS_1 = TypeName.typeNameFromString("foo/egress-1");
    static final TypeName EGRESS_2 = TypeName.typeNameFromString("foo/egress-2");
    static final ValueSpec<String> VALUE_SPEC = ValueSpec.named("foo").withUtf8StringType();

    @Mock
    StatefunServer mockedServer;
    @Captor
    ArgumentCaptor<StatefulFunctions> statefulFunctionsCaptor;

    @Test
    void generatesStringOfFunctionTypesSeparatedBySemicolon() {
        FunctionDefinition functionDefinition = FunctionDefinition.builder()
                .typeName(FunctionUnderTest.TYPE)
                .instance(new FunctionUnderTest())
                .build();
        StatefunModule statefunModule = StatefunModule.builder()
                .functionUnderTest(functionDefinition)
                .collaborator(COLLABORATOR_1)
                .build();
        String expected = FunctionUnderTest.TYPE.asTypeNameString() + ";" + COLLABORATOR_1.asTypeNameString();

        String actual = statefunModule.generateFunctionsString();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generatesStringOfEgressesSeparatedBySemicolon() {
        FunctionDefinition functionDefinition = FunctionDefinition.builder()
                .typeName(FunctionUnderTest.TYPE)
                .instance(new FunctionUnderTest())
                .build();
        StatefunModule statefunModule = StatefunModule.builder()
                .functionUnderTest(functionDefinition)
                .egress(EGRESS_1)
                .egress(EGRESS_2)
                .build();
        String expected = EGRESS_1.asTypeNameString() + ";" + EGRESS_2.asTypeNameString();

        String actual = statefunModule.generateEgressesString();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void returnsStateAccessor() {
        StatefulFunction functionUnderTest = new FunctionUnderTest();
        List<StateSetter<?>> stateSetters = List.of(StateSetterImpl.of(VALUE_SPEC, null));
        FunctionDefinition functionDefinition = FunctionDefinition.builder()
                .typeName(FunctionUnderTest.TYPE)
                .instance(functionUnderTest)
                .stateSetters(stateSetters)
                .build();
        StatefunModule statefunModule = StatefunModule.builder()
                .functionUnderTest(functionDefinition)
                .collaborator(COLLABORATOR_1)
                .collaborator(COLLABORATOR_2)
                .build();

        ManagedStateAccessor stateAccessor = statefunModule.getStateAccessor();

        assertThat(stateAccessor).isNotNull();
    }

    @Test
    void setsEmptyEgressesWhenNullIsGiven() {
        StatefunModule statefunModule = StatefunModule.builder()
                .egresses(null)
                .build();

        assertThat(statefunModule.getEgresses()).isEmpty();
    }

    @Test
    void setsEmptyCollaboratorsWhenNullIsGiven() {
        StatefunModule statefunModule = StatefunModule.builder()
                .collaborators(null)
                .build();

        assertThat(statefunModule.getCollaborators().stream()).isEmpty();
    }

    @Test
    void startsModule() {
        StatefulFunction functionUnderTest = new FunctionUnderTest();
        List<StateSetter<?>> stateSetters = List.of(StateSetterImpl.of(VALUE_SPEC, null));
        ManagedStateFunctionWrapper expectedManagedStateWrapper = ManagedStateFunctionWrapper.of(functionUnderTest, stateSetters);
        FunctionDefinition functionDefinition = FunctionDefinition.builder()
                .typeName(FunctionUnderTest.TYPE)
                .instance(functionUnderTest)
                .stateSetters(stateSetters)
                .build();
        StatefunModule module = StatefunModule.builder()
                .functionUnderTest(functionDefinition)
                .collaborator(COLLABORATOR_1)
                .collaborator(COLLABORATOR_2)
                .build();

        module.start(mockedServer);

        then(mockedServer).should().start(statefulFunctionsCaptor.capture());
        assertThat(statefulFunctionsCaptor.getValue().functionSpecs()).hasEntrySatisfying(FunctionUnderTest.TYPE, s -> {
                    assertThat(s.typeName()).isEqualTo(FunctionUnderTest.TYPE);
                    assertThat(s.supplier().get()).isInstanceOf(NextInvocationsInterceptor.class);
                    assertThat(s.supplier().get()).hasFieldOrPropertyWithValue(
                            "functionUnderTest",
                            expectedManagedStateWrapper
                    );
                    assertThat(s.supplier().get()).hasFieldOrPropertyWithValue(
                            "messageCaptureFunction",
                            MessageCaptureFunction.INSTANCE
                    );
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
    void stopsModule() {
        StatefunModule module = StatefunModule.builder()
                .functionUnderTest(FunctionDefinition.builder()
                        .typeName(FunctionUnderTest.TYPE)
                        .instance(new FunctionUnderTest())
                        .build()
                )
                .collaborator(COLLABORATOR_1)
                .egress(EGRESS_1)
                .build();

        module.start(mockedServer);
        module.stop();

        then(mockedServer).should().stop();
    }

    @Test
    void nothingThrownWhenStoppingModuleWithoutServer() {
        StatefunModule module = StatefunModule.builder()
                .functionUnderTest(FunctionDefinition.builder()
                        .typeName(FunctionUnderTest.TYPE)
                        .instance(new FunctionUnderTest())
                        .build()
                )
                .collaborator(COLLABORATOR_1)
                .egress(EGRESS_1)
                .build();

        assertThatNoException().isThrownBy(module::stop);
    }

    @Test
    void throwsStatefunModuleNotStartedExceptionWhenGettingPortOnNotStartedModule() {
        StatefunModule module = StatefunModule.builder()
                .functionUnderTest(FunctionDefinition.builder()
                        .typeName(FunctionUnderTest.TYPE)
                        .instance(new FunctionUnderTest())
                        .build()
                )
                .collaborator(COLLABORATOR_1)
                .egress(EGRESS_1)
                .build();

        assertThatThrownBy(module::getPort)
                .isInstanceOf(StatefunModuleNotStartedException.class)
                .hasMessage("Port is not available until StatefunModule is started");
    }

    @Test
    void returnsServerPort() {
        StatefunModule module = StatefunModule.builder()
                .functionUnderTest(FunctionDefinition.builder()
                        .typeName(FunctionUnderTest.TYPE)
                        .instance(new FunctionUnderTest())
                        .build()
                )
                .collaborator(COLLABORATOR_1)
                .egress(EGRESS_1)
                .build();
        int expectedPort = 1234;
        when(mockedServer.getPort()).thenReturn(expectedPort);

        module.start(mockedServer);
        Integer actualPort = module.getPort();

        assertThat(actualPort).isEqualTo(expectedPort);
    }

    @Test
    void throwsStatefunModuleNotStartedExceptionIfSettingUncaughtExceptionHandlerOnNotStartedModule() {
        StatefunModule module = StatefunModule.builder()
                .functionUnderTest(FunctionDefinition.builder()
                        .typeName(FunctionUnderTest.TYPE)
                        .instance(new FunctionUnderTest())
                        .build()
                )
                .collaborator(COLLABORATOR_1)
                .egress(EGRESS_1)
                .build();

        assertThatThrownBy(() -> module.setUncaughtExceptionHandler(e -> {
        }))
                .isInstanceOf(StatefunModuleNotStartedException.class)
                .hasMessage("Cannot set UncaughtExceptionHandler if StatefunModule is not started");
    }

    @Test
    void setsUncaughtExceptionHandlerIfModuleIsStarted() {
        StatefunModule module = StatefunModule.builder()
                .functionUnderTest(FunctionDefinition.builder()
                        .typeName(FunctionUnderTest.TYPE)
                        .instance(new FunctionUnderTest())
                        .build()
                )
                .collaborator(COLLABORATOR_1)
                .egress(EGRESS_1)
                .build();
        Consumer<Throwable> exceptionHandler = e -> {
        };

        module.start(mockedServer);
        module.setUncaughtExceptionHandler(exceptionHandler);

        then(mockedServer).should().setUncaughtExceptionHandler(exceptionHandler);
    }

    private static class FunctionUnderTest implements StatefulFunction {

        static TypeName TYPE = TypeName.typeNameFromString("foo/function-under-test");

        @Override
        public CompletableFuture<Void> apply(Context context, Message argument) throws Throwable {
            return null;
        }
    }

}