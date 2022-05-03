package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;

public interface StateMatcher extends ChangeMatcher {

    void match(TsukuyomiApi tsukuyomi);

}
