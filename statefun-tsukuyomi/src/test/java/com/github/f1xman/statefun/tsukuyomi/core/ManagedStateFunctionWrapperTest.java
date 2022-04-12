package com.github.f1xman.statefun.tsukuyomi.core;

import org.apache.flink.statefun.sdk.java.AddressScopedStorage;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagedStateFunctionWrapperTest {

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

        wrapper.apply(context, message);

        then(stateSetter).should().setStateValue(storage);
        then(statefulFunction).should().apply(context, message);
    }
}