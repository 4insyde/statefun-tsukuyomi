package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.ManagedStateAccessor;
import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiApi;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.BDDMockito.when;

@ExtendWith(MockitoExtension.class)
class ExpectStateTest {

    static final ValueSpec<String> FOO = ValueSpec.named("foo").withUtf8StringType();

    @Mock
    TsukuyomiApi mockedTsukuyomi;
    @Mock
    ManagedStateAccessor mockedStateAccessor;

    @Test
    void waitsUntilStateMatchesOrTimeoutAndThenAsserts() {
        ExpectState<String> expectState = ExpectState.of(FOO, is("foo"));
        when(mockedTsukuyomi.getStateAccessor()).thenReturn(mockedStateAccessor);
        when(mockedStateAccessor.getStateValue(FOO)).thenReturn(Optional.empty());

        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            Thread currentThread = Thread.currentThread();
            Thread thread = new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    currentThread.interrupt();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            assertThrows(AssertionError.class, () -> expectState.match(0, mockedTsukuyomi));
        });
    }
}