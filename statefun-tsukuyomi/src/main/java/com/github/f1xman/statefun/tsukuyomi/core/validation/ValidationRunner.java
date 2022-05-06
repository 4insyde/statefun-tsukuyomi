package com.github.f1xman.statefun.tsukuyomi.core.validation;

public interface ValidationRunner {
    @Deprecated
    void validate(ChangeMatcher... changeMatchers);

    void validate(Criterion... criteria);
}
