package com.github.insyde.statefun.tsukuyomi.core.capture;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.statefun.sdk.java.StatefulFunctions;
import org.apache.flink.statefun.sdk.java.handler.RequestReplyHandler;
import org.apache.flink.statefun.sdk.java.slice.Slice;
import org.apache.flink.statefun.sdk.java.slice.Slices;

import java.net.ServerSocket;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static io.undertow.UndertowOptions.ENABLE_HTTP2;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class UndertowStatefunServer implements StatefunServer {

    @Getter
    private final int port;
    private Undertow server;
    private UndertowHttpHandler handler;

    @SneakyThrows
    public static UndertowStatefunServer useAvailablePort() {
        @Cleanup
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        return new UndertowStatefunServer(port);
    }

    @Override
    public void start(StatefulFunctions statefulFunctions) {
        handler = new UndertowHttpHandler(statefulFunctions.requestReplyHandler());
        server = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(handler)
                .setServerOption(ENABLE_HTTP2, true)
                .build();
        server.start();
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    public void setUncaughtExceptionHandler(Consumer<Throwable> exceptionHandler) {
        if (handler == null) {
            throw new StatefunServerNotStartedException(
                    "UncaughtExceptionHandler cannot be set until the server is not started");
        }
        handler.setUncaughtExceptionHandler(exceptionHandler);
    }

    @Slf4j
    @RequiredArgsConstructor
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    private static final class UndertowHttpHandler implements HttpHandler {

        RequestReplyHandler handler;
        @NonFinal
        @Setter
        Consumer<Throwable> uncaughtExceptionHandler;

        @Override
        public void handleRequest(HttpServerExchange exchange) {
            exchange.getRequestReceiver().receiveFullBytes(this::onRequestBody);
        }

        private void onRequestBody(HttpServerExchange exchange, byte[] requestBytes) {
            exchange.dispatch();
            CompletableFuture<Slice> future = handler.handle(Slices.wrap(requestBytes));
            future.whenComplete((response, exception) -> onComplete(exchange, response, exception));
        }

        private void onComplete(HttpServerExchange exchange, Slice responseBytes, Throwable ex) {
            if (ex != null) {
                log.error("Function invocation failed", ex);
                exchange.getResponseHeaders().put(Headers.STATUS, 500);
                exchange.endExchange();
                uncaughtExceptionHandler.accept(ex);
                return;
            }
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream");
            exchange.getResponseSender().send(responseBytes.asReadOnlyByteBuffer());
        }
    }
}
