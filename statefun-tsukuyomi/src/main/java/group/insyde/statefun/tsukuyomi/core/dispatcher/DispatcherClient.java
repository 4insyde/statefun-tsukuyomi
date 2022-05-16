package group.insyde.statefun.tsukuyomi.core.dispatcher;

import group.insyde.statefun.tsukuyomi.core.capture.Envelope;
import lombok.SneakyThrows;

import java.util.Collection;

public interface DispatcherClient {
    @SneakyThrows
    void connect();

    void send(Envelope envelope);

    Collection<Envelope> getReceived();
}
