package com.github.f1xman.statefun.tsukuyomi.dsl;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StateSetter;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StateSetterImpl;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StateValue;
import com.github.f1xman.statefun.tsukuyomi.core.validation.*;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.ValueSpec;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class BddTsukuyomi {

    public static GivenFunction given(TypedFunction function, StateSetter<?>... states) {
        if (function == null) {
            throw new NullTypedFunctionException("Use BddTsukuyomi.function(..) to define your function");
        }
        if (states == null) {
            throw new NullStateSettersException("Use BddTsukuyomi.withState(..) to define initial state");
        }
        boolean hasNullStateSetter = Arrays.stream(states)
                .anyMatch(Objects::isNull);
        if (hasNullStateSetter) {
            throw new NullStateSetterException(
                    "At least one StateSetter is null. Use BddTsukuyomi.withState(..) to define initial state");
        }
        return GivenFunctionImpl.of(function, states, new TsukuyomiManagerImpl());
    }

    public static TypedFunction function(TypeName type, StatefulFunction instance) {
        if (type == null) {
            throw new NullFunctionTypeNameException("Function under test must have a TypeName");
        }
        if (instance == null) {
            throw new NullFunctionInstanceException("Function under test must have an instance");
        }
        return TypedFunctionImpl.of(type, instance);
    }

    public static <T> StateSetter<T> withState(ValueSpec<T> spec, StateValue<T> value) {
        if (spec == null) {
            throw new NullValueSpecException("ValueSpec cannot be null");
        }
        if (value == null) {
            throw new NullStateValueException("StateValue cannot be null");
        }
        return StateSetterImpl.of(spec, value.get());
    }

    public static Then when(GivenFunction givenFunction, Interactor interactor) {
        if (givenFunction == null) {
            throw new NullGivenFunctionException(
                    "GivenFunction cannot be null. Use BddTsukuyomi.given(..) to instantiate one");
        }
        if (interactor == null) {
            throw new NullInteractorException(
                    "Interactor cannot be null. Use BddTsukuyomi.receives(..) to instantiate one");
        }
        return Then.of(givenFunction, interactor);
    }

    public static Interactor receives(Envelope envelope) {
        if (envelope == null) {
            throw new NullIncomingEnvelopeException(
                    "The function under test cannot receive null. Use Envelope.builder() to build a message");
        }
        return SendMessageInteractor.of(envelope);
    }

    @RequiredArgsConstructor(staticName = "of")
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    public static class Then {

        GivenFunction function;
        Interactor interactor;

        void then(CriterionFactory... criterionFactories) {
            if (criterionFactories == null) {
                throw new NullExpectationsException(
                        "Nothing to verify. Define your expectations using Criteria.*() in a then(..) block");
            }
            if (criterionFactories.length == 0) {
                throw new MissingExpectationsException(
                        "Nothing to verify. Define your expectations using Criteria.*() in a then(..) block");
            }
            boolean hasNullMatcher = Arrays.stream(criterionFactories)
                    .anyMatch(Objects::isNull);
            if (hasNullMatcher) {
                throw new NullExpectationException(
                        "At least one expectation is null. Define your expectations using Criteria.*() in a then(..) block");
            }
            ValidationRunnerImpl runner = ValidationRunnerImpl.of(function, interactor);
            Criterion[] criteria = IntStream.range(0, criterionFactories.length)
                    .mapToObj(i -> criterionFactories[i].create(i))
                    .toArray(Criterion[]::new);
            runner.validate(criteria);
        }

    }

}
