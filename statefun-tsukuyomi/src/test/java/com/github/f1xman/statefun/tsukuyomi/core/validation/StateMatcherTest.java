package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.ManagedStateAccessor;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StateMatcherTest {

    public static final String FOO = "foo";
    public static final ValueSpec<String> VALUE_SPEC = ValueSpec.named(FOO).withUtf8StringType();
    @Mock
    TsukuyomiApi tsukuyomi;
    @Mock
    ManagedStateAccessor stateAccessor;

    @BeforeEach
    void setUp() {
        given(tsukuyomi.getStateAccessor()).willReturn(stateAccessor);
    }

    @Test
    void throwsAssertionErrorIfValuesNotEqual() {
        StateCriterion criterion = StateCriterion.of(VALUE_SPEC, is(FOO));
        Matcher matcher = StateMatcher.of(List.of(criterion));
        given(stateAccessor.getStateValue(VALUE_SPEC)).willReturn(Optional.empty());

        assertThatThrownBy(() -> matcher.match(tsukuyomi))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void nothingThrownIfValuesEqual() {
        StateCriterion criterion = StateCriterion.of(VALUE_SPEC, is(FOO));
        Matcher matcher = StateMatcher.of(List.of(criterion));
        given(stateAccessor.getStateValue(VALUE_SPEC)).willReturn(Optional.of(FOO));

        assertThatNoException().isThrownBy(() -> matcher.match(tsukuyomi));
    }

    @Test
    void nothingThrownIfValueIsEmptyOptionalAndCriterionExpectNull() {
        StateCriterion criterion = StateCriterion.of(VALUE_SPEC, nullValue(String.class));
        StateMatcher matcher = StateMatcher.of(List.of(criterion));
        given(stateAccessor.getStateValue(VALUE_SPEC)).willReturn(Optional.empty());

        assertThatNoException().isThrownBy(() -> matcher.match(tsukuyomi));
    }
}