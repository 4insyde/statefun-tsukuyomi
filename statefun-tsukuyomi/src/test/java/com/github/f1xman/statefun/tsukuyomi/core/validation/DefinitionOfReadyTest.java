package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Egresses;
import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.InvocationReport;
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

    @Test
    void awaitsUntil2MessagesReceived() {
        DefinitionOfReady definitionOfReady = DefinitionOfReady.getFrom(mockedTsukuyomiApi);
        Envelope reportEnvelope = Envelope.builder()
                .toEgress(Egresses.CAPTURED_MESSAGES)
                .data(InvocationReport.TYPE, InvocationReport.of(2))
                .build();
        given(mockedTsukuyomiApi.getReceived()).willReturn(
                List.of(),
                List.of(reportEnvelope),
                List.of(reportEnvelope, reportEnvelope, reportEnvelope)
        );
        given(mockedTsukuyomiApi.isActive()).willReturn(true);

        definitionOfReady.await();

        then(mockedTsukuyomiApi).should(times(3)).getReceived();
    }

    @Test
    void stopsWaitingWhenThreadIsInterrupted() {
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            DefinitionOfReady definitionOfReady = DefinitionOfReady.getFrom(mockedTsukuyomiApi);
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
    void stopsWaitingWhenTsukuyomiApiDeactivated() {
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            DefinitionOfReady definitionOfReady = DefinitionOfReady.getFrom(mockedTsukuyomiApi);
            given(mockedTsukuyomiApi.getReceived()).willReturn(List.of());
            given(mockedTsukuyomiApi.isActive()).willReturn(true, false);

            definitionOfReady.incrementExpectedEnvelopes();
            definitionOfReady.await();
        });
    }

    @Test
    void doesNothingWhenNoRequirementsSet() {
        DefinitionOfReady definitionOfReady = DefinitionOfReady.getFrom(mockedTsukuyomiApi);
        given(mockedTsukuyomiApi.getReceived()).willReturn(List.of(Envelope.builder()
                .toEgress(Egresses.CAPTURED_MESSAGES)
                .data(InvocationReport.TYPE, InvocationReport.of(0))
                .build()));

        definitionOfReady.await();

        then(mockedTsukuyomiApi).shouldHaveZeroInteractions();
    }

    @Test
    void doesNotWaitIfInvokedAfterInterrupting() {
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            DefinitionOfReady definitionOfReady = DefinitionOfReady.getFrom(mockedTsukuyomiApi);
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
            definitionOfReady.await();
        });
    }
}