package com.github.f1xman.statefun.tsukuyomi.core;

import org.apache.flink.statefun.sdk.java.*;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.testing.TestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagedStateFunctionWrapperTest {

    static final ValueSpec<String> FOO = ValueSpec.named("foo").withUtf8StringType();
    @Mock
    StatefulFunction statefulFunction;
    @Mock
    Context context;
    @Mock
    Message message;
    @Mock
    StateSetter<?> stateSetter;
    @Mock
    AddressScopedStorage storage;

    @Test
    void preparesStateViaStateSettersBeforeFunctionInvocation() throws Throwable {
        ManagedStateFunctionWrapper wrapper = ManagedStateFunctionWrapper.of(statefulFunction, List.of(stateSetter));
        when(context.storage()).thenReturn(storage);
        when(statefulFunction.apply(any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        wrapper.apply(context, message);

        then(stateSetter).should().setStateValue(storage);
        then(statefulFunction).should().apply(context, message);
    }

    @Test
    void returnsUpdatedStateAfterFunctionInvocation() {
        String expected = "bar";
        StateSetter<String> stateSetter = StateSetterImpl.of(FOO, null);
        ManagedStateFunctionWrapper wrapper = ManagedStateFunctionWrapper.of((c, m) -> {
            c.storage().set(FOO, expected);
            return c.done();
        }, List.of(stateSetter));
        TestContext context = TestContext.forTarget(new Address(TypeName.typeNameFromString("foo/bar"), "id"));

        wrapper.apply(context, message);
        Optional<String> actual = wrapper.getStateValue(FOO);

        assertThat(actual).contains(expected);
    }
}