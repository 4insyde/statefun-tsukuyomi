package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.ManagedStateAccessor;
import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiApi;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.when;

@ExtendWith(MockitoExtension.class)
class ExpectStateTest {

    static final ValueSpec<String> FOO = ValueSpec.named("foo").withUtf8StringType();

    @Mock
    TsukuyomiApi mockedTsukuyomi;
    @Mock
    ManagedStateAccessor mockedStateAccessor;
    @Mock
    DefinitionOfReady mockedDefinitionOfReady;

    @Test
    void throwsAssertionErrorIfStateDoesNotMatch() {
        ExpectState<String> expectState = ExpectState.of(FOO, is("foo"));
        when(mockedTsukuyomi.getStateAccessor()).thenReturn(mockedStateAccessor);
        when(mockedStateAccessor.getStateValue(FOO)).thenReturn(Optional.empty());

        assertThrows(AssertionError.class, () -> expectState.match(0, mockedTsukuyomi));
    }

    @Test
    void throwsNothingIfStateMatches() {
        ExpectState<String> expectState = ExpectState.of(FOO, is("foo"));
        when(mockedTsukuyomi.getStateAccessor()).thenReturn(mockedStateAccessor);
        when(mockedStateAccessor.getStateValue(FOO)).thenReturn(Optional.of("foo"));

        expectState.match(0, mockedTsukuyomi);
    }

    @Test
    void requiresStateToBeUpdated() {
        ExpectState<String> expectState = ExpectState.of(FOO, is("foo"));

        expectState.adjustDefinitionOfReady(mockedDefinitionOfReady);

        then(mockedDefinitionOfReady).should().requireUpdatedState();
    }
}