package group.insyde.statefun.tsukuyomi.core.dispatcher;

import group.insyde.statefun.tsukuyomi.core.capture.Envelope;
import group.insyde.statefun.tsukuyomi.core.capture.InvocationReport;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionCompletedWaiterTest {

    @Mock
    TsukuyomiApi mockedTsukuyomiApi;

    @Test
    void awaitsUntil2MessagesReceived() {
        InteractionCompletedWaiter interactionCompletedWaiter = InteractionCompletedWaiter.getFrom(mockedTsukuyomiApi);
        Envelope envelope = envelope();
        given(mockedTsukuyomiApi.getReceived()).willReturn(
                List.of(),
                List.of(envelope, envelope)
        );
        given(mockedTsukuyomiApi.isActive()).willReturn(true);
        InvocationReport report = InvocationReport.of(List.of(envelope, envelope));
        given(mockedTsukuyomiApi.getInvocationReport()).willReturn(Optional.of(report));

        interactionCompletedWaiter.await();

        then(mockedTsukuyomiApi).should(times(2)).getReceived();
    }

    @Test
    void stopsWaitingWhenThreadIsInterrupted() {
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            InteractionCompletedWaiter interactionCompletedWaiter = InteractionCompletedWaiter.getFrom(mockedTsukuyomiApi);
            given(mockedTsukuyomiApi.getReceived()).willReturn(List.of());

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
            interactionCompletedWaiter.await();
        });
    }

    @Test
    void stopsWaitingWhenTsukuyomiApiDeactivated() {
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            InteractionCompletedWaiter interactionCompletedWaiter = InteractionCompletedWaiter.getFrom(mockedTsukuyomiApi);
            given(mockedTsukuyomiApi.getReceived()).willReturn(List.of());
            given(mockedTsukuyomiApi.isActive()).willReturn(true, false);

            interactionCompletedWaiter.await();
        });
    }

    @Test
    void doesNotWaitIfInvokedAfterInterrupting() {
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            InteractionCompletedWaiter interactionCompletedWaiter = InteractionCompletedWaiter.getFrom(mockedTsukuyomiApi);
            given(mockedTsukuyomiApi.getReceived()).willReturn(List.of());

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
            interactionCompletedWaiter.await();
            interactionCompletedWaiter.await();
        });
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .toFunction(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}