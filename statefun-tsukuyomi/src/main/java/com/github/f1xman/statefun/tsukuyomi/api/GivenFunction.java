package com.github.f1xman.statefun.tsukuyomi.api;

public interface GivenFunction {

    void interact(Interactor[] interactors);

    void expect(ChangeMatcher... matchers);

    void shutdown();
}
