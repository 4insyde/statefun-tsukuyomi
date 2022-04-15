package com.github.f1xman.statefun.tsukuyomi.core;

import com.github.f1xman.statefun.tsukuyomi.core.testcontainers.DispatcherContainer;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.output.Slf4jLogConsumer;

@Slf4j
public class TsukuyomiManagerImpl implements TsukuyomiManager {

    static final int STATEFUN_PORT = 9876;
    static final String FUNCTIONS_ENV = "FUNCTIONS";
    static final String ENDPOINT_ENV = "ENDPOINT";
    static final int ORIGINAL_DISPATCHER_PORT = 5555;
    static final String EGRESSES_ENV = "EGRESSES";
    private ModuleServer server;
    private DispatcherContainer dispatcher;

    @Override
    public TsukuyomiApi start(ModuleDefinition moduleDefinition) {
        int statefunPort = STATEFUN_PORT;
        server = ModuleServer.start(statefunPort);
        server.deployModule(moduleDefinition);

        Testcontainers.exposeHostPorts(statefunPort);
        dispatcher = DispatcherContainer.create();
        dispatcher.addEnv(FUNCTIONS_ENV, moduleDefinition.generateFunctionsString());
        dispatcher.addEnv(ENDPOINT_ENV, String.format("http://host.testcontainers.internal:%d", statefunPort));
        dispatcher.addEnv(EGRESSES_ENV, moduleDefinition.generateEgressesString());
        dispatcher.withLogConsumer(new Slf4jLogConsumer(log));
        dispatcher.start();

        Runtime.getRuntime().addShutdownHook(new Thread(dispatcher::stop));

        String host = dispatcher.getHost();
        Integer dispatcherPort = dispatcher.getMappedPort(ORIGINAL_DISPATCHER_PORT);
        SocketDispatcherClient client = new SocketDispatcherClient(host, dispatcherPort);
        client.connect();
        return DispatcherBasedTsukuyomi.of(client, moduleDefinition.getStateAccessor());
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
        if (dispatcher != null) {
            dispatcher.stop();
        }
    }
}
