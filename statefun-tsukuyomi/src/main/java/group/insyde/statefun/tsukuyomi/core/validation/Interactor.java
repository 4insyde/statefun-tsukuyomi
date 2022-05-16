package group.insyde.statefun.tsukuyomi.core.validation;

import group.insyde.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;

public interface Interactor {

    void interact(TsukuyomiApi tsukuyomi);
}
