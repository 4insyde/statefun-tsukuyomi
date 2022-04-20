package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.StatefunModule;
import com.github.f1xman.statefun.tsukuyomi.core.capture.UndertowStatefunServer;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.DispatcherBasedTsukuyomi;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.DispatcherClient;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.DispatcherContainer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TsukuyomiManagerImpl implements TsukuyomiManager {

    private DispatcherContainer dispatcher;
    private StatefunModule statefunModule;

    @Override
    public TsukuyomiApi start(StatefunModule statefunModule) {
        startStatefunServer(statefunModule);
        DispatcherClient dispatcherClient = startDispatcher(statefunModule);
        return DispatcherBasedTsukuyomi.of(dispatcherClient, statefunModule.getStateAccessor());
    }

    private DispatcherClient startDispatcher(StatefunModule statefunModule) {
        dispatcher = DispatcherContainer.builder()
                .statefunPort(statefunModule.getPort())
                .statefunModule(statefunModule)
                .build();
        dispatcher.start();
        DispatcherClient client = dispatcher.createClient();
        client.connect();
        return client;
    }

    private void startStatefunServer(StatefunModule statefunModule) {
        this.statefunModule = statefunModule;
        this.statefunModule.start(UndertowStatefunServer.useAvailablePort());
    }

    @Override
    public void stop() {
        if (statefunModule != null) {
            statefunModule.stop();
        }
        if (dispatcher != null) {
            dispatcher.stop();
        }
    }
}
