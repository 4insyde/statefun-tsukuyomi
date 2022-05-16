package com.github.insyde.statefun.tsukuyomi.core.validation;

import com.github.insyde.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;

public interface Interactor {

    void interact(TsukuyomiApi tsukuyomi);
}
