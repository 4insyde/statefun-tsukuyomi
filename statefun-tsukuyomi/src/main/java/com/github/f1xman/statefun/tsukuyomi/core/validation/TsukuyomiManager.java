package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.ModuleDefinition;

public interface TsukuyomiManager {

    TsukuyomiApi start(ModuleDefinition moduleDefinition);

    void stop();
}
