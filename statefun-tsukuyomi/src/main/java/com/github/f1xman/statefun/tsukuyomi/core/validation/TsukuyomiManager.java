package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.StatefunModule;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;

public interface TsukuyomiManager {

    TsukuyomiApi start(StatefunModule statefunModule);

    void stop();
}
