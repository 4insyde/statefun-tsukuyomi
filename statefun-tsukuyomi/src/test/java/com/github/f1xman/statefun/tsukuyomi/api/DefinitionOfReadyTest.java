package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiApi;
import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class DefinitionOfReadyTest {

    @Mock
    TsukuyomiApi mockedTsukuyomiApi;
    @Mock
    List<Envelope> mockedReceivedEnvelopes;

    @Test
    void awaitsUntil2MessagesReceived() {
        DefinitionOfReady definitionOfReady = DefinitionOfReady.of(mockedTsukuyomiApi);
        given(mockedTsukuyomiApi.getReceived()).willReturn(mockedReceivedEnvelopes);
        given(mockedReceivedEnvelopes.size()).willReturn(0, 2);

        definitionOfReady.incrementExpectedEnvelopes();
        definitionOfReady.incrementExpectedEnvelopes();
        definitionOfReady.await();

        then(mockedReceivedEnvelopes).should(times(2)).size();
    }

    @Test
    void awaitsUntilStateIsUpdated() {
        DefinitionOfReady definitionOfReady = DefinitionOfReady.of(mockedTsukuyomiApi);
        given(mockedTsukuyomiApi.isStateUpdated()).willReturn(false, true);

        definitionOfReady.requireUpdatedState();
        definitionOfReady.await();

        then(mockedTsukuyomiApi).should(times(2)).isStateUpdated();
    }

    @Test
    void stopsWaitingWhenThreadIsInterrupted() {
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            DefinitionOfReady definitionOfReady = DefinitionOfReady.of(mockedTsukuyomiApi);
            given(mockedTsukuyomiApi.getReceived()).willReturn(List.of());

            definitionOfReady.incrementExpectedEnvelopes();
            Thread interruptedThread = Thread.currentThread();
            Thread interrupterThread = new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    interruptedThread.interrupt();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            interrupterThread.start();
            definitionOfReady.await();
        });
    }

    @Test
    void doesNothingWhenNoRequirementsSet() {
        DefinitionOfReady definitionOfReady = DefinitionOfReady.of(mockedTsukuyomiApi);

        definitionOfReady.await();

        then(mockedTsukuyomiApi).shouldHaveZeroInteractions();
    }
}