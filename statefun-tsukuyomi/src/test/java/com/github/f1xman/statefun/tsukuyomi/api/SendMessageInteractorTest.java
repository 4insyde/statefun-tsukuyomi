package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiApi;
import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.types.Type;
import org.apache.flink.statefun.sdk.java.types.TypeSerializer;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

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
        Type<String> stringType = Types.stringType();
        TypeSerializer<String> serializer = stringType.typeSerializer();
        return Envelope.builder()
                .from(Envelope.NodeAddress.of(FOO_BAR, "foobar"))
                .to(Envelope.NodeAddress.of("foo/baz", "foobaz"))
                .data(Envelope.Data.of(
                        stringType.typeName().asTypeNameString(),
                        Base64.getEncoder().encodeToString(serializer.serialize("foobarbaz").toByteArray())
                ))
                .build();
    }
}