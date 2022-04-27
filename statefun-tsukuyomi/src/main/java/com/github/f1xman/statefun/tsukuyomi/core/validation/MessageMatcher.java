package com.github.f1xman.statefun.tsukuyomi.core.validation;

public interface MessageMatcher extends ChangeMatcher {

    void match(int order, TsukuyomiApi tsukuyomi);

    Target getTarget();

}
