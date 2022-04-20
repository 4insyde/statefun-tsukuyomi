package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.StatefunModule;

public interface TsukuyomiManager {

    TsukuyomiApi start(StatefunModule statefunModule);

    void stop();
}
