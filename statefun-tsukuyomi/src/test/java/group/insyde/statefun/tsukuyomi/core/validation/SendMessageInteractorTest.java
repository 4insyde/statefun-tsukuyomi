package group.insyde.statefun.tsukuyomi.core.validation;

import group.insyde.statefun.tsukuyomi.core.capture.Envelope;
import group.insyde.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SendMessageInteractorTest {

    static final TypeName FOO_BAR = TypeName.typeNameFromString("foo/bar");
    static final TypeName FOO_BAZ = TypeName.typeNameFromString("foo/baz");

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
                .from(FOO_BAR, "foobar")
                .toFunction(FOO_BAZ, "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}