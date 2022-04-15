package com.github.f1xman.statefun.tsukuyomi.api;

public interface GivenFunction {

    void start(ChangeMatcher[] matchers);

    void interact(Interactor[] interactors);

    void expect(ChangeMatcher... matchers);

    void stop();
}
