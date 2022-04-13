package com.github.f1xman.statefun.tsukuyomi.dispatcher;

import com.github.f1xman.statefun.tsukuyomi.dispatcher.config.DispatcherConfig;
import com.github.f1xman.statefun.tsukuyomi.dispatcher.config.Server;
import com.github.f1xman.statefun.tsukuyomi.dispatcher.config.StatefunModule;
import com.github.f1xman.statefun.tsukuyomi.dispatcher.job.DispatcherJob;
import com.github.f1xman.statefun.tsukuyomi.dispatcher.socket.DispatcherSocket;
import com.github.f1xman.statefun.tsukuyomi.dispatcher.socket.DispatcherSocketImpl;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.core.execution.JobClient;

import java.net.URI;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class Runner {

    static String FUNCTIONS_ENV = "FUNCTIONS";
    static String ENDPOINT_ENV = "ENDPOINT";
    static String EGRESSES_ENV = "EGRESSES";

    public static void main(String[] args) {
        String functions = System.getenv(FUNCTIONS_ENV);
        String endpoint = System.getenv(ENDPOINT_ENV);
        String egresses = System.getenv(EGRESSES_ENV);
        StatefunModule module = StatefunModule.of(functions, URI.create(endpoint), egresses);

        DispatcherConfig config = DispatcherConfig.of(module);
        DispatcherSocket socket = DispatcherSocketImpl.start(Server.getDefaultInputServer().getPort());
        DispatcherJob dispatcherJob = DispatcherJob.of(config);

        JobClient jobClient = dispatcherJob.start();
        jobClient.getJobStatus()
                .thenAccept(status -> log.info("Job status is {}", status));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            @SneakyThrows
            public void run() {
                log.info("Clearing resources");
                jobClient.cancel().get();
                socket.close();
                log.info("Resources cleared");
            }
        });
    }

}
