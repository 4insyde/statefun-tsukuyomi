package com.github.insyde.statefun.tsukuyomi.core.validation;

public interface GivenFunction {

    void interact(Interactor interactor);

    void expect(Criterion... criteria);

    void stop();

    void start(Criterion... criteria);
}
