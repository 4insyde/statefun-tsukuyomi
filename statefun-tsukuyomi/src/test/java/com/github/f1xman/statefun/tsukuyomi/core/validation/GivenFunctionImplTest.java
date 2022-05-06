package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.*;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GivenFunctionImplTest {

    static final TypeName COLLABORATOR = TypeName.typeNameFromString("foo/collaborator");
    static final TypeName EGRESS = TypeName.typeNameFromString("foo/egress");
    static final String ID = "foobar";
    public static final int INCORRECT_ORDER = Integer.MAX_VALUE;
    public static final int CORRECT_ORDER = 0;

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
    private int order;

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

        then(mockedChangeMatcherA).should().match(eq(0), eq(mockedTsukuyomiApi), any());
        then(mockedChangeMatcherB).should().match(eq(0), eq(mockedTsukuyomiApi), any());
        then(mockedChangeMatcherC).should().match(eq(1), eq(mockedTsukuyomiApi), any());
    }

    @Test
    void throwsAssertionErrorIfMultipleMatchersExpectTheSameEnvelope() {
        GivenFunctionImpl function = GivenFunctionImpl.of(
                TypedFunctionImpl.of(FooBar.TYPE_NAME, new FooBar()),
                new StateSetter[]{},
                mockedTsukuyomiManager
        );
        function.setTsukuyomi(mockedTsukuyomiApi);
        Envelope envelope = envelope();
        given(mockedTsukuyomiApi.getReceived()).willReturn(List.of(envelope));

        assertThatThrownBy(() -> function.expect(
                ExpectMessageInAnyOrder.of(envelope, Target.Type.FUNCTION),
                ExpectMessageInAnyOrder.of(envelope, Target.Type.FUNCTION)
        ));
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

    @Test
    void throwsAssertionErrorIfEnvelopeHasIncorrectOrder() {
        GivenFunctionImpl givenFunction = GivenFunctionImpl.builder()
                .typedFunction(TypedFunctionImpl.of(FooBar.TYPE_NAME, new FooBar()))
                .manager(mockedTsukuyomiManager)
                .tsukuyomi(mockedTsukuyomiApi)
                .build();
        Envelope envelope = envelope();
        givenEnvelopesReceived(envelope);

        assertThatThrownBy(
                () -> givenFunction.expect(EnvelopeCriterion.ordered(INCORRECT_ORDER, envelope)))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void throwsNothingIfEnvelopeHasCorrectOrder() {
        GivenFunctionImpl givenFunction = GivenFunctionImpl.builder()
                .typedFunction(TypedFunctionImpl.of(FooBar.TYPE_NAME, new FooBar()))
                .manager(mockedTsukuyomiManager)
                .tsukuyomi(mockedTsukuyomiApi)
                .build();
        Envelope envelope = envelope();
        givenEnvelopesReceived(envelope);

        assertThatNoException()
                .isThrownBy(() -> givenFunction.expect(EnvelopeCriterion.ordered(CORRECT_ORDER, envelope)));
    }

    @Test
    void throwsAssertionErrorIfFewCriteriaRelatesToTheSameEnvelopeAndTheLastOneIsInvalid() {
        GivenFunctionImpl givenFunction = GivenFunctionImpl.builder()
                .typedFunction(TypedFunctionImpl.of(FooBar.TYPE_NAME, new FooBar()))
                .manager(mockedTsukuyomiManager)
                .tsukuyomi(mockedTsukuyomiApi)
                .build();
        Envelope envelope = envelope();
        givenEnvelopesReceived(envelope);

        assertThatThrownBy(() -> givenFunction.expect(
                EnvelopeCriterion.ordered(CORRECT_ORDER, envelope),
                EnvelopeCriterion.unordered(envelope)
        )).isInstanceOf(AssertionError.class);
    }

    private void givenEnvelopesReceived(Envelope... envelopes) {
        List<Envelope> envelopesList = Arrays.asList(envelopes);
        InvocationReport report = InvocationReport.of(envelopes.length, envelopesList);
        given(mockedTsukuyomiApi.getInvocationReport()).willReturn(Optional.of(report));
        given(mockedTsukuyomiApi.getReceived()).willReturn(envelopesList);
    }

    private Envelope envelope() {
        return Envelope.builder()
                .to(FooBar.TYPE_NAME, ID)
                .data(Types.stringType(), "foobarbaz")
                .build();
    }

    private Envelope envelope1() {
        return Envelope.builder()
                .to(FooBar.TYPE_NAME, ID)
                .data(Types.stringType(), "barbarbar")
                .build();
    }

    private static class FooBar implements StatefulFunction {

        static final TypeName TYPE_NAME = TypeName.typeNameFromString("foo/bar");

        @Override
        public CompletableFuture<Void> apply(Context context, Message argument) throws Throwable {
            return null;
        }
    }

}