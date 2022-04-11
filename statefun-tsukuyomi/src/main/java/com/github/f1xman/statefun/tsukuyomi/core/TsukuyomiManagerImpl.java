package com.github.f1xman.statefun.tsukuyomi.core;

import com.github.f1xman.statefun.tsukuyomi.testcontainers.DispatcherContainer;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.output.Slf4jLogConsumer;

@Slf4j
public class TsukuyomiManagerImpl implements TsukuyomiManager {

    static final int STATEFUN_PORT = 9876;
    static final String FUNCTIONS_ENV = "FUNCTIONS";
    static final String ENDPOINT_ENV = "ENDPOINT";
    static final int ORIGINAL_DISPATCHER_PORT = 5555;

    @Override
    public TsukiyomiApi start(ModuleDefinition moduleDefinition) {
        int statefunPort = STATEFUN_PORT;
        ModuleServer server = ModuleServer.start(statefunPort);
        server.deployModule(moduleDefinition);

        Testcontainers.exposeHostPorts(statefunPort);
        DispatcherContainer dispatcher = DispatcherContainer.create();
        dispatcher.addEnv(FUNCTIONS_ENV, moduleDefinition.generateFunctionsString());
        dispatcher.addEnv(ENDPOINT_ENV, String.format("http://host.testcontainers.internal:%d", statefunPort));
        dispatcher.withLogConsumer(new Slf4jLogConsumer(log));
        dispatcher.start();

        Runtime.getRuntime().addShutdownHook(new Thread(dispatcher::stop));

        String host = dispatcher.getHost();
        Integer dispatcherPort = dispatcher.getMappedPort(ORIGINAL_DISPATCHER_PORT);
        DispatcherClient client = new DispatcherClient(host, dispatcherPort);
        client.connect();
        return client;
    }
}
