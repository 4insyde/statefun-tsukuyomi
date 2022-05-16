package group.insyde.statefun.tsukuyomi.core.validation;

import group.insyde.statefun.tsukuyomi.core.capture.StatefunModule;
import group.insyde.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;

public interface TsukuyomiManager {

    TsukuyomiApi start(StatefunModule statefunModule);

    void stop();
}
