package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SendMessageInteractorTest {

    static final String FOO_BAR = "foo/bar";

    @Mock
    TsukuyomiApi mockedTsukuyomiApi;

    @Test
    void sendsEnvelope() {
        Envelope envelope = envelope();
        SendMessageInteractor interactor = SendMessageInteractor.of(envelope);

        interactor.interact(mockedTsukuyomiApi);

        then(mockedTsukuyomiApi).should().send(envelope);
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(Envelope.NodeAddress.of(FOO_BAR, "foobar"))
                .to(Envelope.NodeAddress.of("foo/baz", "foobaz"))
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}