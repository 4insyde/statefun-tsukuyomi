package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.ModuleDefinition;
import com.github.f1xman.statefun.tsukuyomi.core.capture.ModuleServer;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.DispatcherBasedTsukuyomi;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.DispatcherClient;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.DispatcherContainer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TsukuyomiManagerImpl implements TsukuyomiManager {

    static final int STATEFUN_PORT = 9876;
    private ModuleServer server;
    private DispatcherContainer dispatcher;

    @Override
    public TsukuyomiApi start(ModuleDefinition moduleDefinition) {
        startStatefunServer(moduleDefinition);
        DispatcherClient dispatcherClient = startDispatcher(moduleDefinition);
        return DispatcherBasedTsukuyomi.of(dispatcherClient, moduleDefinition.getStateAccessor());
    }

    private DispatcherClient startDispatcher(ModuleDefinition moduleDefinition) {
        dispatcher = DispatcherContainer.builder()
                .statefunPort(STATEFUN_PORT)
                .moduleDefinition(moduleDefinition)
                .build();
        dispatcher.start();
        DispatcherClient client = dispatcher.createClient();
        client.connect();
        return client;
    }

    private void startStatefunServer(ModuleDefinition moduleDefinition) {
        server = ModuleServer.start(STATEFUN_PORT);
        server.deployModule(moduleDefinition);
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
