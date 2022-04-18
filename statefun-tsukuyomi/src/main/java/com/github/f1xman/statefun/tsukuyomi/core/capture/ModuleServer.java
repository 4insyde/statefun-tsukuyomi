package com.github.f1xman.statefun.tsukuyomi.core.capture;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.statefun.sdk.java.StatefulFunctions;
import org.apache.flink.statefun.sdk.java.handler.RequestReplyHandler;
import org.apache.flink.statefun.sdk.java.slice.Slice;
import org.apache.flink.statefun.sdk.java.slice.Slices;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static io.undertow.UndertowOptions.ENABLE_HTTP2;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@RequiredArgsConstructor(access = PRIVATE)
public class ModuleServer {

    private static final RequestReplyHandler EMPTY = new StatefulFunctions().requestReplyHandler();
    private static Undertow server;
    private final HandlerSupplier handlerSupplier;

    public static ModuleServer start(int port) {
        HandlerSupplier handlerSupplier = new HandlerSupplier(EMPTY);
        server = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(new UndertowHttpHandler(handlerSupplier))
                .setServerOption(ENABLE_HTTP2, true)
                .build();
        server.start();
        return new ModuleServer(handlerSupplier);
    }

    @SneakyThrows
    public static String getHostAddress() {
        return InetAddress.getLocalHost().getHostAddress();
    }

    public void deployModule(ModuleDefinition moduleDefinition) {
        StatefulFunctions statefulFunctions = moduleDefinition.toStatefulFunctions();
        handlerSupplier.setHandler(statefulFunctions.requestReplyHandler());
    }

    public void stop() {
        if (server != null) {
            log.info("Stopping server");
            server.stop();
        }
    }

    @RequiredArgsConstructor
    private static final class UndertowHttpHandler implements HttpHandler {

        private final Supplier<RequestReplyHandler> handlerSupplier;

        @Override
        public void handleRequest(HttpServerExchange exchange) {
            exchange.getRequestReceiver().receiveFullBytes(this::onRequestBody);
        }

        private void onRequestBody(HttpServerExchange exchange, byte[] requestBytes) {
            exchange.dispatch();
            CompletableFuture<Slice> future = handlerSupplier.get().handle(Slices.wrap(requestBytes));
            future.whenComplete((response, exception) -> onComplete(exchange, response, exception));
        }

        private void onComplete(HttpServerExchange exchange, Slice responseBytes, Throwable ex) {
            if (ex != null) {
                log.error("Function invocation failed", ex);
                exchange.getResponseHeaders().put(Headers.STATUS, 500);
                exchange.endExchange();
                return;
            }
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream");
            exchange.getResponseSender().send(responseBytes.asReadOnlyByteBuffer());
        }
    }

    @AllArgsConstructor
    private static class HandlerSupplier implements Supplier<RequestReplyHandler> {

        @Setter
        private RequestReplyHandler handler;

        @Override
        public RequestReplyHandler get() {
            return handler;
        }
    }
}
