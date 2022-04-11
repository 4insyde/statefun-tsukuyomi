package com.github.f1xman.statefun.tsukuyomi.core;

public interface TsukuyomiManager {

    TsukuyomiApi start(ModuleDefinition moduleDefinition);

    void stop();
}
