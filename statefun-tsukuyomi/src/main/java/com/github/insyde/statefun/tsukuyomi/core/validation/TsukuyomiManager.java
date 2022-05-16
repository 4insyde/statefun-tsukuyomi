package com.github.insyde.statefun.tsukuyomi.core.validation;

import com.github.insyde.statefun.tsukuyomi.core.capture.StatefunModule;
import com.github.insyde.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;

public interface TsukuyomiManager {

    TsukuyomiApi start(StatefunModule statefunModule);

    void stop();
}
