package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.validation.ChangeMatcher;

public interface Then {
    void then(ChangeMatcher... changeMatchers);
}
