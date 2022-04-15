package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiApi;

import java.util.Optional;

public interface ChangeMatcher {

    void match(int order, TsukuyomiApi tsukuyomi);

    Optional<Target> getTarget();

    void adjustDefinitionOfReady(DefinitionOfReady definitionOfReady);
}
