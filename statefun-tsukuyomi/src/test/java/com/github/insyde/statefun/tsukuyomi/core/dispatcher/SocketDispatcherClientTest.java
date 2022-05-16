package com.github.insyde.statefun.tsukuyomi.core.dispatcher;

import com.github.insyde.statefun.tsukuyomi.core.capture.Envelope;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
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

class SocketDispatcherClientTest {

    @Test
    void sendsEnvelope() throws IOException {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Envelope expected = Envelope.builder()
                    .from(TypeName.typeNameFromString("foo/from"), "id")
                    .toFunction(TypeName.typeNameFromString("foo/to"), "id")
                    .data(Types.stringType(), "foo")
                    .build();
            @Cleanup
            ServerSocket serverSocket = new ServerSocket(0);
            ExecutorService executor = Executors.newCachedThreadPool();
            AssertableSocket assertableSocket = new AssertableSocket(serverSocket);
            executor.execute(assertableSocket);
            DispatcherClient client = new SocketDispatcherClient(serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());

            client.connect();
            client.send(expected);

            assertableSocket.assertEnvelopeReceived(expected);
        });
    }

    @Test
    void receivesEnvelopesInOrder() throws IOException {
        Envelope expectedEnvelopeFoo = Envelope.builder()
                .from(TypeName.typeNameFromString("foo/from"), "id")
                .toFunction(TypeName.typeNameFromString("foo/to"), "id")
                .data(Types.stringType(), "foo")
                .build();
        Envelope expectedEnvelopeBar = Envelope.builder()
                .from(TypeName.typeNameFromString("bar/from"), "id")
                .toFunction(TypeName.typeNameFromString("bar/to"), "id")
                .data(Types.stringType(), "foo")
                .build();
        Envelope[] expected = {expectedEnvelopeBar, expectedEnvelopeFoo};
        @Cleanup
        ServerSocket serverSocket = new ServerSocket(0);
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(() -> {
            try {
                Socket socket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(expectedEnvelopeBar.toJsonAsString());
                writer.println(expectedEnvelopeFoo.toJsonAsString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        DispatcherClient client = new SocketDispatcherClient(serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());

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