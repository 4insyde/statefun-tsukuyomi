package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.FunctionDefinition;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StateSetter;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StatefunModule;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GivenFunctionImplTest {

    static final TypeName COLLABORATOR = TypeName.typeNameFromString("foo/collaborator");
    static final TypeName EGRESS = TypeName.typeNameFromString("foo/egress");

    @Mock
    TsukuyomiManager mockedTsukuyomiManager;
    @Mock
    StateSetter<String> mockedStateSetter;
    @Mock
    TsukuyomiApi mockedTsukuyomiApi;
    @Mock
    Interactor mockedInteractor;
    @Mock
    MessageMatcher mockedChangeMatcherA;
    @Mock
    MessageMatcher mockedChangeMatcherB;
    @Mock
    MessageMatcher mockedChangeMatcherC;

    @Test
    void startsTsukuyomi() {
        FooBar instance = new FooBar();
        GivenFunctionImpl function = GivenFunctionImpl.of(
                TypedFunctionImpl.of(FooBar.TYPE_NAME, instance),
                new StateSetter[]{mockedStateSetter},
                mockedTsukuyomiManager
        );
        FunctionDefinition functionDefinition = FunctionDefinition.builder()
                .typeName(FooBar.TYPE_NAME)
                .instance(instance)
                .stateSetters(List.of(mockedStateSetter))
                .build();
        StatefunModule statefunModule = StatefunModule.builder()
                .functionUnderTest(functionDefinition)
                .collaborator(COLLABORATOR)
                .egress(EGRESS)
                .build();
        given(mockedChangeMatcherA.getTarget())
                .willReturn(Target.of(COLLABORATOR, Target.Type.FUNCTION));
        given(mockedChangeMatcherB.getTarget())
                .willReturn(Target.of(EGRESS, Target.Type.EGRESS));

        function.start(new ChangeMatcher[]{mockedChangeMatcherA, mockedChangeMatcherB});

        then(mockedTsukuyomiManager).should().start(statefunModule);
    }

    @Test
    void interacts() {
        FooBar instance = new FooBar();
        GivenFunctionImpl function = GivenFunctionImpl.of(
                TypedFunctionImpl.of(FooBar.TYPE_NAME, instance),
                new StateSetter[]{mockedStateSetter},
                mockedTsukuyomiManager
        );
        function.setTsukuyomi(mockedTsukuyomiApi);

        function.interact(mockedInteractor);

        then(mockedInteractor).should().interact(mockedTsukuyomiApi);
    }

    @Test
    void validatesExpectations() {
        FooBar instance = new FooBar();
        GivenFunctionImpl function = GivenFunctionImpl.of(
                TypedFunctionImpl.of(FooBar.TYPE_NAME, instance),
                new StateSetter[]{mockedStateSetter},
                mockedTsukuyomiManager
        );
        function.setTsukuyomi(mockedTsukuyomiApi);
        given(mockedChangeMatcherA.getTarget())
                .willReturn(Target.of(COLLABORATOR, Target.Type.FUNCTION));
        given(mockedChangeMatcherB.getTarget())
                .willReturn(Target.of(EGRESS, Target.Type.EGRESS));
        given(mockedChangeMatcherC.getTarget())
                .willReturn(Target.of(COLLABORATOR, Target.Type.FUNCTION));

        function.expect(mockedChangeMatcherA, mockedChangeMatcherB, mockedChangeMatcherC);

        then(mockedChangeMatcherA).should().match(0, mockedTsukuyomiApi);
        then(mockedChangeMatcherB).should().match(0, mockedTsukuyomiApi);
        then(mockedChangeMatcherC).should().match(1, mockedTsukuyomiApi);
    }

    @Test
    void stopsTsukuyomi() {
        FooBar instance = new FooBar();
        GivenFunctionImpl function = GivenFunctionImpl.of(
                TypedFunctionImpl.of(FooBar.TYPE_NAME, instance),
                new StateSetter[]{mockedStateSetter},
                mockedTsukuyomiManager
        );

        function.stop();

        then(mockedTsukuyomiManager).should().stop();
    }

    private static class FooBar implements StatefulFunction {

        static final TypeName TYPE_NAME = TypeName.typeNameFromString("foo/bar");

        @Override
        public CompletableFuture<Void> apply(Context context, Message argument) throws Throwable {
            return null;
        }
    }

}