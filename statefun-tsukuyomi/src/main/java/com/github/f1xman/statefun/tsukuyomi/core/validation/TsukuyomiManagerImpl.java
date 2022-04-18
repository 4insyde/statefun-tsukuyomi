package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.ModuleDefinition;
import com.github.f1xman.statefun.tsukuyomi.core.capture.ModuleServer;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.DispatcherBasedTsukuyomi;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.DispatcherContainer;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.SocketDispatcherClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TsukuyomiManagerImpl implements TsukuyomiManager {

    static final int STATEFUN_PORT = 9876;
    static final int ORIGINAL_DISPATCHER_PORT = 5555;
    private ModuleServer server;
    private DispatcherContainer dispatcher;

    @Override
    public TsukuyomiApi start(ModuleDefinition moduleDefinition) {
        int statefunPort = STATEFUN_PORT;
        server = ModuleServer.start(statefunPort);
        server.deployModule(moduleDefinition);

        dispatcher = DispatcherContainer.builder()
                .statefunPort(statefunPort)
                .moduleDefinition(moduleDefinition)
                .build();
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
