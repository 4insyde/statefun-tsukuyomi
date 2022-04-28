package com.github.f1xman.statefun.tsukuyomi.core.dispatcher;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Egresses;
import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.InvocationReport;
import com.github.f1xman.statefun.tsukuyomi.core.capture.ManagedStateAccessor;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class DispatcherBasedTsukuyomiTest {

    @Mock
    DispatcherClient mockedClient;
    @Mock
    ManagedStateAccessor mockedStateAccessor;

    @Test
    void sendsEnvelope() {
        DispatcherBasedTsukuyomi tsukuyomi = DispatcherBasedTsukuyomi.of(mockedClient, null, () -> true);
        Envelope envelope = Envelope.builder()
                .from(TypeName.typeNameFromString("foo/from"), "from")
                .to(TypeName.typeNameFromString("foo/to"), "to")
                .data(Types.stringType(), "foobar")
                .build();

        tsukuyomi.send(envelope);

        then(mockedClient).should().send(envelope);
    }

    @Test
    void getsReceived() {
        DispatcherBasedTsukuyomi tsukuyomi = DispatcherBasedTsukuyomi.of(mockedClient, null, () -> true);
        Envelope envelope = Envelope.builder()
                .from(TypeName.typeNameFromString("foo/from"), "from")
                .to(TypeName.typeNameFromString("foo/to"), "to")
                .data(Types.stringType(), "foobar")
                .build();
        List<Envelope> expected = List.of(envelope);
        when(mockedClient.getReceived()).thenReturn(expected);

        Collection<Envelope> actual = tsukuyomi.getReceived();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void receivedDoesNotContainTheReport() {
        DispatcherBasedTsukuyomi tsukuyomi = DispatcherBasedTsukuyomi.of(mockedClient, null, () -> true);
        Envelope envelope = Envelope.builder()
                .toEgress(Egresses.CAPTURED_MESSAGES)
                .data(InvocationReport.TYPE, InvocationReport.of(0))
                .build();
        List<Envelope> expected = List.of(envelope);
        when(mockedClient.getReceived()).thenReturn(expected);

        Collection<Envelope> actual = tsukuyomi.getReceived();

        assertThat(actual).isEmpty();
    }

    @Test
    void delegatesIsStateUpdatedCall() {
        DispatcherBasedTsukuyomi tsukuyomi = DispatcherBasedTsukuyomi.of(mockedClient, mockedStateAccessor, () -> true);
        given(mockedStateAccessor.isStateUpdated()).willReturn(true, false);

        assertThat(tsukuyomi.isStateUpdated()).isTrue();
        assertThat(tsukuyomi.isStateUpdated()).isFalse();
    }

    @Test
    void isActiveReturnsActivityStatusSupplierValue() {
        DispatcherBasedTsukuyomi tsukuyomi = DispatcherBasedTsukuyomi.of(mockedClient, mockedStateAccessor, () -> true);

        boolean actualActive = tsukuyomi.isActive();

        assertThat(actualActive).isTrue();
    }

    @Test
    void returnsOptionalWithInvocationReportIfPresent() {
        DispatcherBasedTsukuyomi tsukuyomi = DispatcherBasedTsukuyomi.of(mockedClient, null, () -> true);
        InvocationReport expected = InvocationReport.of(0);
        Envelope envelope = Envelope.builder()
                .toEgress(Egresses.CAPTURED_MESSAGES)
                .data(InvocationReport.TYPE, expected)
                .build();
        List<Envelope> received = List.of(envelope);
        when(mockedClient.getReceived()).thenReturn(received);

        Optional<InvocationReport> actual = tsukuyomi.getInvocationReport();

        assertThat(actual).contains(expected);
    }
}