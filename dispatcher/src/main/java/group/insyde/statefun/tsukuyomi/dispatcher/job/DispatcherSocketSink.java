package group.insyde.statefun.tsukuyomi.dispatcher.job;

import group.insyde.statefun.tsukuyomi.dispatcher.socket.DispatcherSocket;
import group.insyde.statefun.tsukuyomi.dispatcher.socket.DispatcherSocketHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.io.PrintWriter;
import java.io.Serializable;

@Slf4j
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
        log.info("Writing {}", value);
        writer.println(value.toJson());
        log.info("Written successfully");
    }

    @Override
    public void finish() throws Exception {
        if (writer != null) {
            writer.close();
        }
    }
}
