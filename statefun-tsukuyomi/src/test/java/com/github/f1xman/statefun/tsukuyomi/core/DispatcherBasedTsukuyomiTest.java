package com.github.f1xman.statefun.tsukuyomi.core;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.when;

@ExtendWith(MockitoExtension.class)
class DispatcherBasedTsukuyomiTest {

    @Mock
    DispatcherClient mockedClient;

    @Test
    void sendsEnvelope() {
        DispatcherBasedTsukuyomi tsukuyomi = DispatcherBasedTsukuyomi.of(mockedClient, null);
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
        DispatcherBasedTsukuyomi tsukuyomi = DispatcherBasedTsukuyomi.of(mockedClient, null);
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
}