package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.*;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GivenFunctionImplTest {

    static final TypeName COLLABORATOR = TypeName.typeNameFromString("foo/collaborator");

    @Mock
    TsukuyomiManager mockedTsukuyomiManager;
    @Mock
    TsukuyomiApi mockedTsukuyomiApi;
    @Mock
    Interactor mockedInteractor;
    @Mock
    ChangeMatcher mockedChangeMatcherA;
    @Mock
    ChangeMatcher mockedChangeMatcherB;

    @Test
    void startsManagerAndInteractsWithTsukuyomiApi() {
        FooBar fooBar = new FooBar();
        GivenFunctionImpl givenFunction = GivenFunctionImpl.of(
                TypedFunctionImpl.of(FooBar.TYPE_NAME, fooBar),
                new StateSetter[]{StateSetterImpl.of(FooBar.BAZ, "baz")},
                mockedTsukuyomiManager
        );
        ModuleDefinition moduleDefinition = ModuleDefinition.builder()
                .functionUnderTest(ModuleDefinition.FunctionDefinition.builder()
                        .typeName(FooBar.TYPE_NAME)
                        .instance(fooBar)
                        .stateSetters(List.of(StateSetterImpl.of(FooBar.BAZ, "baz")))
                        .build())
                .collaborators(List.of(COLLABORATOR))
                .build();
        given(mockedTsukuyomiManager.start(moduleDefinition)).willReturn(mockedTsukuyomiApi);
        given(mockedInteractor.getCollaborator()).willReturn(Optional.of(COLLABORATOR));

        givenFunction.interact(mockedInteractor);

        then(mockedInteractor).should().interact(mockedTsukuyomiApi);
    }

    @Test
    void throwsIllegalStateExceptionIfInteractInvokedMoreThanOnce() {
        FooBar fooBar = new FooBar();
        GivenFunctionImpl givenFunction = GivenFunctionImpl.of(
                TypedFunctionImpl.of(FooBar.TYPE_NAME, fooBar),
                new StateSetter[]{StateSetterImpl.of(FooBar.BAZ, "baz")},
                mockedTsukuyomiManager
        );
        given(mockedTsukuyomiManager.start(any())).willReturn(mockedTsukuyomiApi);

        assertThatThrownBy(() -> {
            givenFunction.interact();
            givenFunction.interact();
        })
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Already interacted. GivenFunction cannot be reused.");
    }

    @Test
    void throwsIllegalStateExceptionExpectInvokedBeforeInteract() {
        FooBar fooBar = new FooBar();
        GivenFunctionImpl givenFunction = GivenFunctionImpl.of(
                TypedFunctionImpl.of(FooBar.TYPE_NAME, fooBar),
                new StateSetter[]{StateSetterImpl.of(FooBar.BAZ, "baz")},
                mockedTsukuyomiManager
        );

        assertThatThrownBy(givenFunction::expect)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("GivenFunction.interact(..) was not invoked");
    }

    @Test
    void invokesMatchersInGivenOrder() {
        FooBar fooBar = new FooBar();
        GivenFunctionImpl givenFunction = GivenFunctionImpl.of(
                TypedFunctionImpl.of(FooBar.TYPE_NAME, fooBar),
                new StateSetter[]{StateSetterImpl.of(FooBar.BAZ, "baz")},
                mockedTsukuyomiManager
        );
        given(mockedTsukuyomiManager.start(any(ModuleDefinition.class))).willReturn(mockedTsukuyomiApi);

        givenFunction.interact(mockedInteractor);
        givenFunction.expect(mockedChangeMatcherA, mockedChangeMatcherB);

        then(mockedChangeMatcherA).should().match(eq(0), any());
        then(mockedChangeMatcherB).should().match(eq(1), any());
    }

    @Test
    void stopsTsukuyomiManager() {
        FooBar fooBar = new FooBar();
        GivenFunctionImpl givenFunction = GivenFunctionImpl.of(
                TypedFunctionImpl.of(FooBar.TYPE_NAME, fooBar),
                new StateSetter[]{StateSetterImpl.of(FooBar.BAZ, "baz")},
                mockedTsukuyomiManager
        );

        givenFunction.shutdown();

        then(mockedTsukuyomiManager).should().stop();
    }

    private static class FooBar implements StatefulFunction {

        static final TypeName TYPE_NAME = TypeName.typeNameFromString("foo/bar");
        static final ValueSpec<String> BAZ = ValueSpec.named("baz").withUtf8StringType();

        @Override
        public CompletableFuture<Void> apply(Context context, Message argument) throws Throwable {
            return null;
        }
    }

}