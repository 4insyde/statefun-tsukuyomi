package com.github.insyde.statefun.tsukuyomi.dsl;

import com.github.insyde.statefun.tsukuyomi.core.capture.Envelope;
import com.github.insyde.statefun.tsukuyomi.core.capture.StateSetter;
import com.github.insyde.statefun.tsukuyomi.core.validation.TypedFunction;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static com.github.insyde.statefun.tsukuyomi.core.capture.StateValue.empty;
import static com.github.insyde.statefun.tsukuyomi.dsl.BddTsukuyomi.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class BddTsukuyomiTest {

    static final TypeName COLLABORATOR_1 = TypeName.typeNameFromString("foo/collaborator-1");
    static final TypeName EGRESS = TypeName.typeNameFromString("foo/egress");
    static final String HELLO = "hello";
    static final String FUNCTION_ID = "functionId";
    static final String BAR = "bar";

    @Test
    void throwsNullTypedFunctionExceptionIfPassingNull() {
        assertThatThrownBy(() -> given(null))
                .isInstanceOf(NullTypedFunctionException.class)
                .hasMessage("Use BddTsukuyomi.function(..) to define your function");
    }

    @Test
    void throwsNullStateSettersExceptionIfPassingNull() {
        assertThatThrownBy(() -> given(mock(TypedFunction.class), null))
                .isInstanceOf(NullStateSettersException.class)
                .hasMessage("Use BddTsukuyomi.withState(..) to define initial state");
    }

    @Test
    void throwsNullStateSetterExceptionIfPassingNull() {
        assertThatThrownBy(() -> given(mock(TypedFunction.class), new StateSetter[]{null}))
                .isInstanceOf(NullStateSetterException.class)
                .hasMessage("At least one StateSetter is null. Use BddTsukuyomi.withState(..) to define initial state");
    }

    @Test
    void throwsNullFunctionTypeNameExceptionIfPassingNull() {
        assertThatThrownBy(() -> function(null, new Testee()))
                .isInstanceOf(NullFunctionTypeNameException.class)
                .hasMessage("Function under test must have a TypeName");
    }

    @Test
    void throwsNullFunctionInstanceExceptionIfPassingNull() {
        assertThatThrownBy(() -> function(Testee.TYPE, null))
                .isInstanceOf(NullFunctionInstanceException.class)
                .hasMessage("Function under test must have an instance");
    }

    @Test
    void throwsNullValueSpecExceptionIfPassingNull() {
        assertThatThrownBy(() -> withState(null, empty()))
                .isInstanceOf(NullValueSpecException.class)
                .hasMessage("ValueSpec cannot be null");
    }

    @Test
    void throwsNullStateValueExceptionIfPassingNull() {
        assertThatThrownBy(() -> withState(Testee.BAR, null))
                .isInstanceOf(NullStateValueException.class)
                .hasMessage("StateValue cannot be null");
    }

    @Test
    void throwsNullGivenFunctionExceptionIfPassingNull() {
        assertThatThrownBy(() -> when(null, receives(outgoingEnvelopeToEgress())))
                .isInstanceOf(NullGivenFunctionException.class)
                .hasMessage("GivenFunction cannot be null. Use BddTsukuyomi.given(..) to instantiate one");
    }

    @Test
    void throwsNullInteractorExceptionIfPassingNull() {
        assertThatThrownBy(() -> when(given(function(Testee.TYPE, new Testee())), null))
                .isInstanceOf(NullInteractorException.class)
                .hasMessage("Interactor cannot be null. Use BddTsukuyomi.receives(..) to instantiate one");
    }

    @Test
    void throwsNullIncomingEnvelopeExceptionIfPassingNull() {
        assertThatThrownBy(() -> receives(null))
                .isInstanceOf(NullIncomingEnvelopeException.class)
                .hasMessage("The function under test cannot receive null. Use Envelope.builder() to build a message");
    }

    @Test
    void throwsMissingExpectationsExceptionIfNothingPassed() {
        assertThatThrownBy(() -> when(given(function(Testee.TYPE, new Testee())), receives(incomingEnvelope())).then())
                .isInstanceOf(MissingExpectationsException.class)
                .hasMessage("Nothing to verify. Define your expectations using Criteria.*() in a then(..) block");
    }

    @Test
    void throwsNullExpectationsExceptionIfNullPassed() {
        assertThatThrownBy(() -> when(given(function(Testee.TYPE, new Testee())), receives(incomingEnvelope())).then(null))
                .isInstanceOf(NullExpectationsException.class)
                .hasMessage("Nothing to verify. Define your expectations using Criteria.*() in a then(..) block");
    }

    private Envelope outgoingEnvelopeToEgress() {
        return Envelope.builder()
                .toEgress(EGRESS)
                .data(Types.stringType(), HELLO + BAR)
                .build();
    }

    private Envelope incomingEnvelope() {
        return Envelope.builder()
                .from(COLLABORATOR_1, FUNCTION_ID)
                .toFunction(Testee.TYPE, FUNCTION_ID)
                .data(Types.stringType(), HELLO)
                .build();
    }

    static class Testee implements StatefulFunction {

        static TypeName TYPE = TypeName.typeNameFromString("foo/testee");
        static ValueSpec<String> BAR = ValueSpec.named("bar").withUtf8StringType();

        @Override
        public CompletableFuture<Void> apply(Context context, Message message) {
            return context.done();
        }
    }
}