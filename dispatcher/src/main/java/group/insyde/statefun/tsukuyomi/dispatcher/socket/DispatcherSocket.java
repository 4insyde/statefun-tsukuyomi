package group.insyde.statefun.tsukuyomi.dispatcher.socket;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;

public interface DispatcherSocket extends Closeable {
    CompletableFuture<BufferedReader> getReader();

    CompletableFuture<PrintWriter> getWriter();
}
