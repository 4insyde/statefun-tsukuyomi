package com.github.insyde.statefun.tsukuyomi.dispatcher.job;

import com.github.insyde.statefun.tsukuyomi.dispatcher.socket.DispatcherSocket;
import com.github.insyde.statefun.tsukuyomi.dispatcher.socket.DispatcherSocketHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.io.BufferedReader;
import java.io.Serializable;

@Slf4j
class DispatcherSocketSource extends RichSourceFunction<Envelope> implements Serializable {

    private volatile boolean cancelled;
    private transient BufferedReader reader;

    @Override
    public void open(Configuration parameters) throws Exception {
        DispatcherSocket socket = DispatcherSocketHolder.getSocket();
        reader = socket.getReader().get();
        log.info("Client connected, start reading...");
    }

    @Override
    public void run(SourceContext<Envelope> ctx) throws Exception {
        while (!cancelled) {
            String line = reader.readLine();
            if (line != null) {
                log.info("Line received: {}", line);
                Envelope envelope = Envelope.fromJson(line);
                ctx.collect(envelope);
            }
        }

    }

    @Override
    public void cancel() {
        cancelled = true;
    }
}
