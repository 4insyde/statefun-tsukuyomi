package com.github.f1xman.statefun.tsukuyomi;

import com.github.f1xman.statefun.tsukuyomi.capture.Envelope;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class DispatcherClient implements TsukiyomiApi {

    Queue<Envelope> received = new ConcurrentLinkedQueue<>();

    String host;
    int port;
    @NonFinal
    Socket socket;
    @NonFinal
    PrintWriter writer;

    @SneakyThrows
    public void connect() {
        socket = new Socket(host, port);
        writer = new PrintWriter(socket.getOutputStream(), true);
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(() -> {
            try {
                @Cleanup
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("Line received: {}", line);
                    Envelope envelope = Envelope.fromJson(line);
                    received.add(envelope);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void send(Envelope envelope) {
        String json = envelope.toJsonAsString();
        writer.println(json);
        log.info("Line sent: {}", json);
    }

    @Override
    public Collection<Envelope> getReceived() {
        return Collections.unmodifiableCollection(received);
    }

}
