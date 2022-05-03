package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.StatefunModule;
import com.github.f1xman.statefun.tsukuyomi.core.capture.UndertowStatefunServer;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.DispatcherBasedTsukuyomi;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.DispatcherClient;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.DispatcherContainer;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class TsukuyomiManagerImpl implements TsukuyomiManager {

    private final AtomicBoolean active = new AtomicBoolean();
    private DispatcherContainer dispatcher;
    private StatefunModule statefunModule;

    @Override
    public TsukuyomiApi start(StatefunModule statefunModule) {
        startStatefunModule(statefunModule);
        DispatcherClient dispatcherClient = startDispatcher(statefunModule);
        active.set(true);
        return DispatcherBasedTsukuyomi.of(dispatcherClient, statefunModule.getStateAccessor(), active::get);
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

    private void startStatefunModule(StatefunModule statefunModule) {
        this.statefunModule = statefunModule;
        this.statefunModule.start(UndertowStatefunServer.useAvailablePort());
        this.statefunModule.setUncaughtExceptionHandler(e -> {
            log.warn("Exception occurred during function invocation, stopping Tsukuyomi...", e);
            CompletableFuture.runAsync(this::stop);
        });
    }

    @Override
    public void stop() {
        active.set(false);
        if (statefunModule != null) {
            statefunModule.stop();
        }
        if (dispatcher != null) {
            dispatcher.stop();
        }
    }
}
