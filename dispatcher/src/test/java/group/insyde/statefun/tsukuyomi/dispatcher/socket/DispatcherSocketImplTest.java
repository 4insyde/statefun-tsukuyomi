package group.insyde.statefun.tsukuyomi.dispatcher.socket;

import lombok.Cleanup;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class DispatcherSocketImplTest {

    static final int PORT = 6789;
    static final String LOCALHOST = "localhost";
    static final Duration TIMEOUT = Duration.ofSeconds(3);

    @Test
    void readsFromClient() {
        assertTimeoutPreemptively(TIMEOUT, () -> {
            String expected = "foo";
            @Cleanup
            DispatcherSocket socket = DispatcherSocketImpl.start(PORT);
            @Cleanup
            Socket client = new Socket(LOCALHOST, PORT);

            CompletableFuture<BufferedReader> futureReader = socket.getReader();

            futureReader.thenAccept(reader -> assertDoesNotThrow(() -> {
                new PrintWriter(client.getOutputStream(), true).println(expected);
                String line = reader.readLine();
                assertThat(line).isEqualTo(expected);
            }));
        });
    }

    @Test
    void writesToClient() {
        assertTimeoutPreemptively(TIMEOUT, () -> {
            String expected = "foo";
            @Cleanup
            DispatcherSocket socket = DispatcherSocketImpl.start(PORT);
            @Cleanup
            Socket client = new Socket(LOCALHOST, PORT);

            CompletableFuture<PrintWriter> futureWriter = socket.getWriter();

            futureWriter.thenAccept(writer -> assertDoesNotThrow(() -> {
                writer.println(expected);
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line = reader.readLine();
                assertThat(line).isEqualTo(expected);
            }));
        });
    }
}