package com.github.f1xman.statefun.tsukuyomi.core.validation;

public interface StateMatcher extends ChangeMatcher {

    void match(TsukuyomiApi tsukuyomi);

}
