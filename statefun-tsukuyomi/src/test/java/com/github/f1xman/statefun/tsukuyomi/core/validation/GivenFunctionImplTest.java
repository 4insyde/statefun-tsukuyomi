package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.*;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.ValueSpec;
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
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GivenFunctionImplTest {

    public static final int INCORRECT_ORDER = Integer.MAX_VALUE;
    public static final int CORRECT_ORDER = 0;
    public static final ValueSpec<String> FOO_VALUE_SPEC = ValueSpec.named("foo").withUtf8StringType();
    public static final String FOO = "foo";
    public static final String BAR = "bar";
    public static final TypeName ANOTHER_TYPE_NAME = TypeName.typeNameFromString("foo/another");
    public static final TypeName EGRESS_TYPE_NAME = TypeName.typeNameFromString("foo/egress");
    static final TypeName COLLABORATOR = TypeName.typeNameFromString("foo/collaborator");
    static final String ID = "foobar";
    static final TypeName EGRESS = EGRESS_TYPE_NAME;

    @Mock
    TsukuyomiManager mockedTsukuyomiManager;
    @Mock
    StateSetter<String> mockedStateSetter;
    @Mock
    TsukuyomiApi mockedTsukuyomiApi;
    @Mock
    Interactor mockedInteractor;
    @Mock
    ManagedStateAccessor mockedStateAccessor;

    @Test
    void startsTsukuyomiFromCriteria() {
        FooBar instance = new FooBar();
        StateSetter<String> stateSetter = StateSetterImpl.of(FOO_VALUE_SPEC, FOO);
        GivenFunctionImpl givenFunction = GivenFunctionImpl.builder()
                .typedFunction(TypedFunctionImpl.of(FooBar.TYPE_NAME, instance))
                .manager(mockedTsukuyomiManager)
                .tsukuyomi(mockedTsukuyomiApi)
                .stateSetters(new StateSetter[]{stateSetter})
                .build();
        StatefunModule expectedStatefunModule = StatefunModule.builder()
                .functionUnderTest(FunctionDefinition.builder()
                        .typeName(FooBar.TYPE_NAME)
                        .instance(instance)
                        .stateSetter(stateSetter)
                        .build())
                .collaborator(ANOTHER_TYPE_NAME)
                .egress(EGRESS_TYPE_NAME)
                .build();

        givenFunction.start(
                EnvelopeCriterion.of(envelopeToAnotherFunction()),
                EnvelopeCriterion.of(envelopeToEgress())
        );

        then(mockedTsukuyomiManager).should().start(expectedStatefunModule);
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
                () -> givenFunction.expect(EnvelopeCriterion.ofOrdered(INCORRECT_ORDER, envelope)))
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
                .isThrownBy(() -> givenFunction.expect(EnvelopeCriterion.ofOrdered(CORRECT_ORDER, envelope)));
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
                EnvelopeCriterion.ofOrdered(CORRECT_ORDER, envelope),
                EnvelopeCriterion.of(envelope)
        )).isInstanceOf(AssertionError.class);
    }

    @Test
    void throwsAssertionErrorIfExpectedStateDoesNotMatch() {
        GivenFunctionImpl givenFunction = GivenFunctionImpl.builder()
                .typedFunction(TypedFunctionImpl.of(FooBar.TYPE_NAME, new FooBar()))
                .manager(mockedTsukuyomiManager)
                .tsukuyomi(mockedTsukuyomiApi)
                .build();
        given(mockedTsukuyomiApi.getStateAccessor()).willReturn(mockedStateAccessor);
        given(mockedStateAccessor.getStateValue(FOO_VALUE_SPEC)).willReturn(Optional.of(FOO));

        assertThatThrownBy(() -> givenFunction.expect(StateCriterion.of(FOO_VALUE_SPEC, is(BAR))))
                .isInstanceOf(AssertionError.class);
    }

    private void givenEnvelopesReceived(Envelope... envelopes) {
        List<Envelope> envelopesList = Arrays.asList(envelopes);
        InvocationReport report = InvocationReport.of(envelopesList);
        given(mockedTsukuyomiApi.getInvocationReport()).willReturn(Optional.of(report));
        given(mockedTsukuyomiApi.getReceived()).willReturn(envelopesList);
    }

    private Envelope envelope() {
        return Envelope.builder()
                .toFunction(FooBar.TYPE_NAME, ID)
                .data(Types.stringType(), "foobarbaz")
                .build();
    }

    private Envelope envelope1() {
        return Envelope.builder()
                .toFunction(FooBar.TYPE_NAME, ID)
                .data(Types.stringType(), "barbarbar")
                .build();
    }

    private Envelope envelopeToAnotherFunction() {
        return Envelope.builder()
                .toFunction(ANOTHER_TYPE_NAME, ID)
                .data(Types.stringType(), "foobarbaz")
                .build();
    }

    private Envelope envelopeToEgress() {
        return Envelope.builder()
                .toEgress(EGRESS_TYPE_NAME)
                .data(Types.stringType(), "foobarbaz")
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