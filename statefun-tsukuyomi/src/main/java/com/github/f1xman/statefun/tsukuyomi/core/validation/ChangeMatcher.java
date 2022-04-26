package com.github.f1xman.statefun.tsukuyomi.core.validation;

import java.util.Optional;

public interface ChangeMatcher {

    void match(int order, TsukuyomiApi tsukuyomi);

    Optional<Target> getTarget();

}
