package group.insyde.statefun.tsukuyomi.dispatcher.socket;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class DispatcherSocketImpl implements DispatcherSocket {

    ServerSocket serverSocket;
    CompletableFuture<Socket> clientFuture;

    @SneakyThrows
    public static DispatcherSocket start(int port) {
        ServerSocket serverSocket = new ServerSocket(port);
        CompletableFuture<Socket> clientFuture = new CompletableFuture<>();
        clientFuture.completeAsync(() -> {
            try {
                return serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, Executors.newCachedThreadPool());
        DispatcherSocketImpl socket = new DispatcherSocketImpl(serverSocket, clientFuture);
        DispatcherSocketHolder.setSocket(socket);
        return socket;
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }

    @Override
    public CompletableFuture<BufferedReader> getReader() {
        return clientFuture.thenApply(s -> {
            try {
                return new BufferedReader(new InputStreamReader(s.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<PrintWriter> getWriter() {
        return clientFuture.thenApply(s -> {
            try {
                return new PrintWriter(s.getOutputStream(), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
