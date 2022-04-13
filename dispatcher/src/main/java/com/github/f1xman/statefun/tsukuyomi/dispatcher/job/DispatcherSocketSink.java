package com.github.f1xman.statefun.tsukuyomi.dispatcher.job;

import com.github.f1xman.statefun.tsukuyomi.dispatcher.socket.DispatcherSocket;
import com.github.f1xman.statefun.tsukuyomi.dispatcher.socket.DispatcherSocketHolder;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.io.PrintWriter;
import java.io.Serializable;

class DispatcherSocketSink extends RichSinkFunction<Envelope> implements Serializable {

    private transient PrintWriter writer;

    @Override
    public void open(Configuration parameters) throws Exception {
        if (writer == null) {
            DispatcherSocket socket = DispatcherSocketHolder.getSocket();
            writer = socket.getWriter().get();
        }
    }

    @Override
    public void invoke(Envelope value, Context context) throws Exception {
        writer.println(value.toJson());
    }

    @Override
    public void finish() throws Exception {
        if (writer != null) {
            writer.close();
        }
    }
}
