package com.github.f1xman.statefun.tsukuyomi.core;

import com.github.f1xman.statefun.tsukuyomi.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.DispatcherClient;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class DispatcherClientTest {

    @Test
    void sendsEnvelope() throws IOException {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Envelope expected = Envelope.builder()
                    .from(Envelope.NodeAddress.of("foo", "id"))
                    .build();
            @Cleanup
            ServerSocket serverSocket = new ServerSocket(0);
            ExecutorService executor = Executors.newCachedThreadPool();
            AssertableSocket assertableSocket = new AssertableSocket(serverSocket);
            executor.execute(assertableSocket);
            DispatcherClient client = new DispatcherClient(serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());

            client.connect();
            client.send(expected);

            assertableSocket.assertEnvelopeReceived(expected);
        });
    }

    @Test
    void receivesEnvelopes() throws IOException {
        Envelope expectedEnvelopeFoo = Envelope.builder()
                .from(Envelope.NodeAddress.of("foo", "id"))
                .build();
        Envelope expectedEnvelopeBar = Envelope.builder()
                .from(Envelope.NodeAddress.of("bar", "id"))
                .build();
        Envelope[] expected = {expectedEnvelopeFoo, expectedEnvelopeBar};
        @Cleanup
        ServerSocket serverSocket = new ServerSocket(0);
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(() -> {
            try {
                Socket socket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(expectedEnvelopeFoo.toJsonAsString());
                writer.println(expectedEnvelopeBar.toJsonAsString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        DispatcherClient client = new DispatcherClient(serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());

        client.connect();
        Collection<Envelope> actual = client.getReceived();

        await().atMost(5, TimeUnit.SECONDS).until(() -> actual.size() >= 2);
        assertThat(actual).containsExactly(expected);
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    private static class AssertableSocket implements Runnable {

        ServerSocket serverSocket;
        @NonFinal
        volatile Envelope envelope;

        @Override
        public void run() {
            try {
                @Cleanup
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line = reader.readLine();
                envelope = Envelope.fromJson(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void assertEnvelopeReceived(Envelope expected) {
            while (envelope == null) {
                Thread.onSpinWait();
            }
            assertThat(envelope).isEqualTo(expected);
        }
    }
}