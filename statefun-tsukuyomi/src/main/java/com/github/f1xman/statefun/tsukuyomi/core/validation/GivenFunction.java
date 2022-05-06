package com.github.f1xman.statefun.tsukuyomi.core.validation;

public interface GivenFunction {

    void start(ChangeMatcher[] matchers);

    void interact(Interactor interactor);

    void expect(ChangeMatcher... matchers);

    void expect(Criterion... criteria);

    void stop();
}
